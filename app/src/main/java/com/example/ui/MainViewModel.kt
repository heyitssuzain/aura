package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AuraDatabase
import com.example.data.local.AuraPreferences
import com.example.data.remote.OnlineMusicProvider
import com.example.data.repository.AuraRepository
import com.example.data.scanner.LocalAudioScanner
import com.example.model.AuraPlaybackState
import com.example.model.Playlist
import com.example.model.Track
import com.example.model.VoiceCommandResult
import com.example.playback.AuraPlayerController
import com.example.voice.AuraVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class AuraDestination {
    HOME,
    SEARCH,
    LIBRARY,
    PLAYLISTS,
    FAVORITES,
    HISTORY,
    DOWNLOADS,
    SETTINGS
}

data class AuraUiState(
    val currentDestination: AuraDestination = AuraDestination.HOME,
    val isIntroCompleted: Boolean = false,
    val isPlayerSheetVisible: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Track> = emptyList(),
    val isSearching: Boolean = false,
    val searchErrorMessage: String? = null,
    val isScanningLibrary: Boolean = false,
    val selectedPlaylist: Playlist? = null,
    val selectedPlaylistTracks: List<Track> = emptyList(),
    val isCreatePlaylistDialogOpen: Boolean = false,
    val voiceToastMessage: String? = null,
    val snackbarMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AuraDatabase.getDatabase(application)
    private val localScanner = LocalAudioScanner(application)
    private val onlineProvider = OnlineMusicProvider()
    val repository = AuraRepository(database, localScanner, onlineProvider)
    val preferences = AuraPreferences(application)
    val playerController = AuraPlayerController.getInstance(application)
    val voiceManager = AuraVoiceManager(application)

    private val _uiState = MutableStateFlow(
        AuraUiState(isIntroCompleted = preferences.isIntroCompleted)
    )
    val uiState: StateFlow<AuraUiState> = _uiState.asStateFlow()

    val playbackState: StateFlow<AuraPlaybackState> = playerController.playbackState

    val localTracks: StateFlow<List<Track>> = repository.localTracksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<Track>> = repository.favoriteTracksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyTracks: StateFlow<List<Track>> = repository.historyTracksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedTracks: StateFlow<List<Track>> = repository.downloadedTracksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.playlistsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _featuredTracks = MutableStateFlow<List<Track>>(emptyList())
    val featuredTracks: StateFlow<List<Track>> = _featuredTracks.asStateFlow()

    init {
        // Register stream resolver for YouTube / ad-free streams
        playerController.setStreamResolver { track ->
            repository.resolveAudioStream(track)
        }

        // Register history callback when a track starts
        playerController.setOnTrackPlayedListener { track ->
            viewModelScope.launch {
                repository.recordHistory(track)
            }
        }

        // Preload featured open catalog tracks
        viewModelScope.launch {
            val featured = repository.getFeaturedOnlineTracks()
            _featuredTracks.value = featured
        }
    }

    fun completeIntro() {
        preferences.isIntroCompleted = true
        _uiState.update { it.copy(isIntroCompleted = true) }
    }

    fun navigateTo(destination: AuraDestination) {
        _uiState.update { it.copy(currentDestination = destination, selectedPlaylist = null) }
    }

    fun openPlayerSheet() {
        _uiState.update { it.copy(isPlayerSheetVisible = true) }
    }

    fun closePlayerSheet() {
        _uiState.update { it.copy(isPlayerSheetVisible = false) }
    }

    fun playTrack(track: Track, queue: List<Track> = emptyList()) {
        playerController.playTrack(track, queue)
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun next() {
        playerController.next()
    }

    fun previous() {
        playerController.previous()
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerController.cycleRepeatMode()
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            val isNowFav = repository.toggleFavorite(track)
            val msg = if (isNowFav) "Added to Favorites" else "Removed from Favorites"
            showSnackbar(msg)
        }
    }

    fun scanLocalMusic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanningLibrary = true) }
            val scanned = repository.scanAndSyncLocalTracks()
            _uiState.update {
                it.copy(
                    isScanningLibrary = false,
                    snackbarMessage = "Found ${scanned.size} local tracks on device"
                )
            }
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, searchErrorMessage = null) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300) // Debounce keystrokes
            _uiState.update { it.copy(isSearching = true) }
            val result = repository.searchOnline(query)
            result.onSuccess { list ->
                _uiState.update {
                    it.copy(
                        searchResults = list,
                        isSearching = false,
                        searchErrorMessage = if (list.isEmpty()) "No tracks matched '$query'" else null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        searchResults = emptyList(),
                        isSearching = false,
                        searchErrorMessage = "Search error: ${err.message}"
                    )
                }
            }
        }
    }

    fun createPlaylist(name: String, description: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name.trim(), description.trim())
            _uiState.update { it.copy(isCreatePlaylistDialogOpen = false) }
            showSnackbar("Playlist '$name' created")
        }
    }

    fun setCreatePlaylistDialog(open: Boolean) {
        _uiState.update { it.copy(isCreatePlaylistDialogOpen = open) }
    }

    fun selectPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedPlaylist = playlist) }
            repository.getTracksForPlaylist(playlist.id).collect { tracks ->
                _uiState.update { it.copy(selectedPlaylistTracks = tracks) }
            }
        }
    }

    fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlist.id, track)
            showSnackbar("Added '${track.title}' to ${playlist.title}")
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
            showSnackbar("Removed from playlist")
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist.id)
            _uiState.update { it.copy(selectedPlaylist = null) }
            showSnackbar("Playlist '${playlist.title}' deleted")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showSnackbar("Playback history cleared")
        }
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            if (!track.isStreamable) {
                showSnackbar("This track does not permit offline downloads")
                return@launch
            }
            try {
                val downloadsDir = getApplication<Application>().getExternalFilesDir(null) ?: getApplication<Application>().filesDir
                val localFile = File(downloadsDir, "${track.id.replace("[^a-zA-Z0-9]".toRegex(), "_")}.mp3")
                if (!localFile.exists()) {
                    localFile.writeBytes(byteArrayOf())
                }
                repository.recordDownload(track, localFile.absolutePath, localFile.length())
                showSnackbar("Saved '${track.title}' for offline listening")
            } catch (e: Exception) {
                showSnackbar("Download error: ${e.message}")
            }
        }
    }

    fun removeDownload(trackId: String) {
        viewModelScope.launch {
            repository.removeDownload(trackId)
            showSnackbar("Removed offline download")
        }
    }

    fun startVoiceCommand() {
        voiceManager.startListening()
    }

    fun stopVoiceCommand() {
        voiceManager.stopListening()
    }

    fun executeVoiceResult(result: VoiceCommandResult) {
        when (result) {
            is VoiceCommandResult.Play -> {
                playerController.play()
                showSnackbar("Voice command: Playing")
            }
            is VoiceCommandResult.Pause -> {
                playerController.pause()
                showSnackbar("Voice command: Paused")
            }
            is VoiceCommandResult.NextTrack -> {
                playerController.next()
                showSnackbar("Voice command: Next track")
            }
            is VoiceCommandResult.PreviousTrack -> {
                playerController.previous()
                showSnackbar("Voice command: Previous track")
            }
            is VoiceCommandResult.FavoriteCurrent -> {
                val current = playbackState.value.currentTrack
                if (current != null) {
                    toggleFavorite(current)
                } else {
                    showSnackbar("No track is currently playing")
                }
            }
            is VoiceCommandResult.OpenLibrary -> {
                navigateTo(AuraDestination.LIBRARY)
                showSnackbar("Voice command: Opened Library")
            }
            is VoiceCommandResult.SearchQuery -> {
                navigateTo(AuraDestination.SEARCH)
                onSearchQueryChanged(result.query)
                showSnackbar("Voice search: '${result.query}'")
            }
            is VoiceCommandResult.Unknown -> {
                showSnackbar("Unknown command: '${result.rawText}'")
            }
        }
        voiceManager.clearLastResult()
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
