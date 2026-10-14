package com.example.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.model.AuraPlaybackState
import com.example.model.PlayerRepeatMode
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AuraPlayerController private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var exoPlayer: ExoPlayer? = null
    private var positionPollJob: Job? = null

    private val _playbackState = MutableStateFlow(AuraPlaybackState())
    val playbackState: StateFlow<AuraPlaybackState> = _playbackState.asStateFlow()

    private var onTrackPlayedListener: ((Track) -> Unit)? = null
    private var streamResolver: (suspend (Track) -> String?)? = null

    // Player listener MUST be initialized before ensurePlayerInitialized() is called in init
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> {
                    _playbackState.update { it.copy(isBuffering = true) }
                }
                Player.STATE_READY -> {
                    val duration = exoPlayer?.duration?.takeIf { it > 0 } ?: 0L
                    _playbackState.update {
                        it.copy(
                            isBuffering = false,
                            durationMs = duration,
                            errorMessage = null
                        )
                    }
                }
                Player.STATE_ENDED -> {
                    _playbackState.update { it.copy(isPlaying = false, isBuffering = false) }
                    handleTrackEnded()
                }
                Player.STATE_IDLE -> {
                    _playbackState.update { it.copy(isBuffering = false) }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startPositionPolling()
            } else {
                stopPositionPolling()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackState.update {
                it.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = "Playback error: ${error.message ?: "Source unavailable"}"
                )
            }
        }
    }

    init {
        ensurePlayerInitialized()
    }

    fun setOnTrackPlayedListener(listener: (Track) -> Unit) {
        onTrackPlayedListener = listener
    }

    fun setStreamResolver(resolver: suspend (Track) -> String?) {
        streamResolver = resolver
    }

    private fun ensurePlayerInitialized() {
        if (exoPlayer == null) {
            try {
                val player = ExoPlayer.Builder(appContext)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                            .setUsage(C.USAGE_MEDIA)
                            .build(),
                        true
                    )
                    .setHandleAudioBecomingNoisy(true)
                    .build()

                attachPlayer(player)
            } catch (e: Exception) {
                _playbackState.update {
                    it.copy(errorMessage = "Player initialization error: ${e.message}")
                }
            }
        }
    }

    fun attachPlayer(player: ExoPlayer) {
        exoPlayer?.removeListener(playerListener)
        exoPlayer = player
        player.addListener(playerListener)
    }

    private fun startPositionPolling() {
        positionPollJob?.cancel()
        positionPollJob = scope.launch {
            while (isActive) {
                val pos = exoPlayer?.currentPosition ?: 0L
                val dur = exoPlayer?.duration?.takeIf { it > 0 } ?: _playbackState.value.durationMs
                _playbackState.update {
                    it.copy(
                        currentPositionMs = pos,
                        durationMs = dur
                    )
                }
                delay(300)
            }
        }
    }

    private fun stopPositionPolling() {
        positionPollJob?.cancel()
        positionPollJob = null
    }

    fun playTrack(track: Track, newQueue: List<Track> = emptyList()) {
        ensurePlayerInitialized()
        val player = exoPlayer ?: return

        val currentQueue = if (newQueue.isNotEmpty()) newQueue else {
            val q = _playbackState.value.queue
            if (q.contains(track)) q else q + track
        }
        val targetIndex = currentQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        _playbackState.update {
            it.copy(
                currentTrack = track,
                queue = currentQueue,
                queueIndex = targetIndex,
                errorMessage = null,
                currentPositionMs = 0L,
                durationMs = track.durationMs
            )
        }

        scope.launch {
            var mediaUriToPlay = track.mediaUri
            if (mediaUriToPlay.startsWith("yt:") || (!mediaUriToPlay.startsWith("http") && !mediaUriToPlay.startsWith("content"))) {
                _playbackState.update { it.copy(isBuffering = true) }
                val resolved = streamResolver?.invoke(track)
                if (!resolved.isNullOrEmpty()) {
                    mediaUriToPlay = resolved
                } else {
                    _playbackState.update {
                        it.copy(
                            isBuffering = false,
                            errorMessage = "Ad-free stream extraction unavailable for '${track.title}'. Please check connection."
                        )
                    }
                    return@launch
                }
            }

            try {
                val mediaMetadata = MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(track.artworkUrl?.let { Uri.parse(it) })
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setMediaId(track.id)
                    .setUri(mediaUriToPlay)
                    .setMediaMetadata(mediaMetadata)
                    .build()

                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()

                onTrackPlayedListener?.invoke(track.copy(mediaUri = mediaUriToPlay))
            } catch (e: Exception) {
                _playbackState.update {
                    it.copy(isBuffering = false, errorMessage = "Cannot play stream: ${e.message}")
                }
            }
        }
    }

    fun play() {
        ensurePlayerInitialized()
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun togglePlayPause() {
        if (_playbackState.value.isPlaying) {
            pause()
        } else {
            if (_playbackState.value.currentTrack != null) {
                play()
            } else {
                val first = _playbackState.value.queue.firstOrNull()
                if (first != null) playTrack(first)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _playbackState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun next() {
        val state = _playbackState.value
        val queue = state.queue
        if (queue.isEmpty()) return

        val nextIndex = if (state.shuffleEnabled && queue.size > 1) {
            (queue.indices - state.queueIndex).randomOrNull() ?: 0
        } else {
            (state.queueIndex + 1) % queue.size
        }

        val nextTrack = queue.getOrNull(nextIndex) ?: return
        playTrack(nextTrack, queue)
    }

    fun previous() {
        val state = _playbackState.value
        val queue = state.queue
        if (queue.isEmpty()) return

        // If played more than 3 seconds, restart current track
        if (state.currentPositionMs > 3000) {
            seekTo(0)
            return
        }

        val prevIndex = if (state.queueIndex > 0) state.queueIndex - 1 else queue.size - 1
        val prevTrack = queue.getOrNull(prevIndex) ?: return
        playTrack(prevTrack, queue)
    }

    private fun handleTrackEnded() {
        val state = _playbackState.value
        when (state.repeatMode) {
            PlayerRepeatMode.ONE -> {
                seekTo(0)
                play()
            }
            PlayerRepeatMode.ALL -> {
                next()
            }
            PlayerRepeatMode.OFF -> {
                if (state.queueIndex < state.queue.size - 1) {
                    next()
                }
            }
        }
    }

    fun toggleShuffle() {
        _playbackState.update { it.copy(shuffleEnabled = !it.shuffleEnabled) }
    }

    fun cycleRepeatMode() {
        _playbackState.update {
            val nextMode = when (it.repeatMode) {
                PlayerRepeatMode.OFF -> PlayerRepeatMode.ALL
                PlayerRepeatMode.ALL -> PlayerRepeatMode.ONE
                PlayerRepeatMode.ONE -> PlayerRepeatMode.OFF
            }
            it.copy(repeatMode = nextMode)
        }
    }

    fun addToQueue(track: Track) {
        _playbackState.update {
            it.copy(queue = it.queue + track)
        }
    }

    fun clearQueue() {
        _playbackState.update {
            it.copy(queue = emptyList(), queueIndex = -1)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AuraPlayerController? = null

        fun getInstance(context: Context): AuraPlayerController {
            return INSTANCE ?: synchronized(this) {
                val instance = AuraPlayerController(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
