package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.AuraPlaybackState
import com.example.model.PlayerRepeatMode
import com.example.model.Track
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraCyanGlow
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceVariant
import com.example.ui.theme.AuraElectricBlue
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerModalSheet(
    playbackState: AuraPlaybackState,
    isFavorite: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onDownload: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onDismiss() }

    val track = playbackState.currentTrack ?: return
    val context = LocalContext.current
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragPosition by remember { mutableFloatStateOf(0f) }
    var showQueueDrawer by remember { mutableStateOf(false) }

    // 3D Turntable & Space Tilt Animation
    val infiniteTransition = rememberInfiniteTransition(label = "Vinyl3DAnimations")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TurntableAngle"
    )

    val tiltX by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = -14f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PerspectiveTiltX"
    )

    val tiltY by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PerspectiveTiltY"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AudioPulseScale"
    )

    val visualizerWavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    Surface(
        color = AuraDarkBackground,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("player_sheet_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (track.sourceProvider == "YOUTUBE") "STREAMING FROM YOUTUBE" else "PLAYING FROM UNIVERSE",
                        color = AuraCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = track.album,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { showQueueDrawer = !showQueueDrawer },
                    modifier = Modifier.testTag("player_sheet_queue_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Toggle Queue",
                        tint = if (showQueueDrawer) AuraCyan else TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (showQueueDrawer) {
                // Queue Drawer View
                Text(
                    text = "UP NEXT (${playbackState.queue.size} TRACKS)",
                    color = AuraCyanGlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(playbackState.queue) { index, item ->
                        val isCurrent = index == playbackState.queueIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCurrent) AuraDarkSurfaceVariant else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = if (isCurrent) AuraCyan else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = if (isCurrent) AuraCyan else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = item.artist,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = item.formattedDuration,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                // Central 3D Turntable / Anime Artwork Disc with Perspective Tilt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                rotationX = if (playbackState.isPlaying) tiltX else 10f
                                rotationY = if (playbackState.isPlaying) tiltY else 0f
                                cameraDistance = 14f * density
                                scaleX = if (playbackState.isPlaying) pulseScale else 1f
                                scaleY = if (playbackState.isPlaying) pulseScale else 1f
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer Holographic Glowing Halo
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            AuraCyan.copy(alpha = 0.35f),
                                            AuraViolet.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(AuraCyan, AuraViolet, AuraElectricBlue, AuraCyan)
                                    ),
                                    shape = CircleShape
                                )
                        )

                        // Rotating 3D Vinyl Disc
                        Box(
                            modifier = Modifier
                                .size(244.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0A0C14))
                                .rotate(if (playbackState.isPlaying) rotationAngle else 0f)
                                .border(8.dp, Color(0xFF161A28), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!track.artworkUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(track.artworkUrl)
                                        .crossfade(true)
                                        .error(R.drawable.img_aura_hero)
                                        .placeholder(R.drawable.img_aura_hero)
                                        .build(),
                                    contentDescription = "${track.title} artwork",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(174.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_aura_hero),
                                    contentDescription = "Aura Character Art",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(174.dp)
                                        .clip(CircleShape)
                                )
                            }

                            // Center Vinyl Spindle Pin
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AuraDarkBackground)
                                    .border(2.5.dp, AuraCyan, CircleShape)
                            )
                        }
                    }
                }

                // Reactive Audio Visualizer Frequency Equalizer Wave
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    val barCount = 28
                    val spacing = size.width / barCount
                    val barWidth = spacing * 0.55f

                    for (i in 0 until barCount) {
                        val factor = if (playbackState.isPlaying) {
                            val wave1 = sin(i.toFloat() * 0.45f + visualizerWavePhase).coerceAtLeast(0f)
                            val wave2 = sin(i.toFloat() * 0.7f - visualizerWavePhase * 1.3f).coerceAtLeast(0f)
                            ((wave1 + wave2) / 2f).coerceIn(0.15f, 1f)
                        } else {
                            0.08f
                        }

                        val barHeight = size.height * factor
                        val x = i * spacing + (spacing - barWidth) / 2f
                        val y = size.height - barHeight

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(AuraCyan, AuraViolet)
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Track Info & Favorite Heart Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = track.artist,
                                color = AuraCyanGlow,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (track.sourceProvider == "YOUTUBE") {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AD-FREE STREAM",
                                    color = AuraCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { onToggleFavorite(track) },
                        modifier = Modifier.testTag("player_sheet_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove Favorite" else "Add Favorite",
                            tint = if (isFavorite) AuraError else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Seekbar Scrubber
                val currentPosition = if (isDraggingSlider) {
                    sliderDragPosition
                } else {
                    playbackState.currentPositionMs.toFloat()
                }
                val duration = playbackState.durationMs.toFloat().coerceAtLeast(1f)

                Slider(
                    value = currentPosition.coerceIn(0f, duration),
                    onValueChange = {
                        isDraggingSlider = true
                        sliderDragPosition = it
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        onSeek(sliderDragPosition.toLong())
                    },
                    valueRange = 0f..duration,
                    colors = SliderDefaults.colors(
                        thumbColor = AuraCyan,
                        activeTrackColor = AuraCyan,
                        inactiveTrackColor = AuraDarkSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_sheet_slider")
                )

                // Time Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = playbackState.formattedPosition,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = playbackState.formattedDuration,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Playback Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (playbackState.shuffleEnabled) AuraCyan else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.testTag("player_sheet_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = TextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Main Glowing Play/Pause FAB
                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = AuraCyan,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .size(68.dp)
                            .testTag("player_sheet_play_pause_button")
                    ) {
                        if (playbackState.isBuffering) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(30.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.testTag("player_sheet_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = TextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(onClick = onCycleRepeat) {
                        val repeatIcon = when (playbackState.repeatMode) {
                            PlayerRepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        }
                        val repeatTint = when (playbackState.repeatMode) {
                            PlayerRepeatMode.OFF -> TextSecondary
                            else -> AuraCyan
                        }
                        Icon(
                            imageVector = repeatIcon,
                            contentDescription = "Repeat Mode",
                            tint = repeatTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Tools (Playlist, Download)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { onAddToPlaylist(track) }) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Add to Playlist",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = { onDownload(track) }) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Download Track",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
