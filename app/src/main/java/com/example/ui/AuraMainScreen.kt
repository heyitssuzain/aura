package com.example.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Track
import com.example.ui.components.MiniPlayer
import com.example.ui.components.PlayerModalSheet
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val featuredTracks by viewModel.featuredTracks.collectAsStateWithLifecycle()
    val historyTracks by viewModel.historyTracks.collectAsStateWithLifecycle()
    val localTracks by viewModel.localTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val downloadedTracks by viewModel.downloadedTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val isVoiceListening by viewModel.voiceManager.isListening.collectAsStateWithLifecycle()
    val lastVoiceResult by viewModel.voiceManager.lastVoiceResult.collectAsStateWithLifecycle()
    val voiceError by viewModel.voiceManager.speechError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle voice speech results
    LaunchedEffect(lastVoiceResult) {
        lastVoiceResult?.let { result ->
            viewModel.executeVoiceResult(result)
        }
    }

    // Handle voice error toast
    LaunchedEffect(voiceError) {
        voiceError?.let { err ->
            viewModel.showSnackbar(err)
        }
    }

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Audio recording permission launcher for Voice Search & Commands
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceCommand()
        } else {
            viewModel.showSnackbar("Microphone permission required for voice commands")
        }
    }

    // First Launch: Auth-Free Anime Landing Screen
    if (!uiState.isIntroCompleted) {
        LandingScreen(
            onEnterAura = { viewModel.completeIntro() },
            modifier = modifier
        )
        return
    }

    // Back handler for sub-destinations
    if (uiState.currentDestination != AuraDestination.HOME && !uiState.isPlayerSheetVisible) {
        BackHandler { viewModel.navigateTo(AuraDestination.HOME) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AuraDarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AuraDarkSurfaceVariant)
                                .border(1.dp, AuraCyan.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SpatialAudio,
                                contentDescription = "Aura Universe",
                                tint = AuraCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (uiState.currentDestination) {
                                AuraDestination.HOME -> "AURA"
                                AuraDestination.SEARCH -> "SEARCH"
                                AuraDestination.LIBRARY -> "LIBRARY"
                                AuraDestination.PLAYLISTS -> "PLAYLISTS"
                                AuraDestination.FAVORITES -> "FAVORITES"
                                AuraDestination.HISTORY -> "HISTORY"
                                AuraDestination.DOWNLOADS -> "DOWNLOADS"
                                AuraDestination.SETTINGS -> "SETTINGS"
                            },
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                actions = {
                    // Voice Command Action Button
                    IconButton(
                        onClick = { recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        modifier = Modifier.testTag("app_bar_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Commands",
                            tint = if (isVoiceListening) AuraCyan else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AuraDarkBackground
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mini Player docked right above bottom navigation bar
                if (playbackState.currentTrack != null && !uiState.isPlayerSheetVisible) {
                    MiniPlayer(
                        playbackState = playbackState,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.next() },
                        onClick = { viewModel.openPlayerSheet() }
                    )
                }

                // Material 3 Navigation Bar
                NavigationBar(
                    containerColor = AuraDarkSurface,
                    contentColor = TextSecondary,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.currentDestination == AuraDestination.HOME,
                        onClick = { viewModel.navigateTo(AuraDestination.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Discover", fontSize = 11.sp) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = uiState.currentDestination == AuraDestination.SEARCH,
                        onClick = { viewModel.navigateTo(AuraDestination.SEARCH) },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search", fontSize = 11.sp) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_search")
                    )
                    NavigationBarItem(
                        selected = uiState.currentDestination == AuraDestination.LIBRARY,
                        onClick = { viewModel.navigateTo(AuraDestination.LIBRARY) },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                        label = { Text("Library", fontSize = 11.sp) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_library")
                    )
                    NavigationBarItem(
                        selected = uiState.currentDestination == AuraDestination.PLAYLISTS,
                        onClick = { viewModel.navigateTo(AuraDestination.PLAYLISTS) },
                        icon = { Icon(Icons.Default.QueueMusic, contentDescription = "Playlists") },
                        label = { Text("Playlists", fontSize = 11.sp) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_playlists")
                    )
                    NavigationBarItem(
                        selected = uiState.currentDestination == AuraDestination.SETTINGS,
                        onClick = { viewModel.navigateTo(AuraDestination.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontSize = 11.sp) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.currentDestination) {
                AuraDestination.HOME -> HomeScreen(
                    featuredTracks = featuredTracks,
                    historyTracks = historyTracks,
                    currentPlayingId = playbackState.currentTrack?.id,
                    onTrackSelected = { track, list -> viewModel.playTrack(track, list) },
                    onNavigate = { dest -> viewModel.navigateTo(dest) },
                    onToggleFavorite = { track -> viewModel.toggleFavorite(track) }
                )
                AuraDestination.SEARCH -> SearchScreen(
                    searchQuery = uiState.searchQuery,
                    searchResults = uiState.searchResults,
                    isSearching = uiState.isSearching,
                    errorMessage = uiState.searchErrorMessage,
                    isVoiceListening = isVoiceListening,
                    currentPlayingId = playbackState.currentTrack?.id,
                    onQueryChanged = { q -> viewModel.onSearchQueryChanged(q) },
                    onVoiceClick = { recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onTrackSelected = { track, list -> viewModel.playTrack(track, list) },
                    onToggleFavorite = { track -> viewModel.toggleFavorite(track) }
                )
                AuraDestination.LIBRARY -> LibraryScreen(
                    localTracks = localTracks,
                    isScanning = uiState.isScanningLibrary,
                    currentPlayingId = playbackState.currentTrack?.id,
                    onScanRequested = { viewModel.scanLocalMusic() },
                    onTrackSelected = { track, list -> viewModel.playTrack(track, list) },
                    onToggleFavorite = { track -> viewModel.toggleFavorite(track) }
                )
                AuraDestination.PLAYLISTS -> PlaylistsScreen(
                    playlists = playlists,
                    selectedPlaylist = uiState.selectedPlaylist,
                    selectedPlaylistTracks = uiState.selectedPlaylistTracks,
                    isCreateDialogOpen = uiState.isCreatePlaylistDialogOpen,
                    currentPlayingId = playbackState.currentTrack?.id,
                    onSelectPlaylist = { p -> viewModel.selectPlaylist(p) },
                    onBackFromPlaylistDetail = { viewModel.navigateTo(AuraDestination.PLAYLISTS) },
                    onCreatePlaylistClicked = { viewModel.setCreatePlaylistDialog(true) },
                    onDismissCreateDialog = { viewModel.setCreatePlaylistDialog(false) },
                    onCreatePlaylistConfirmed = { name, desc -> viewModel.createPlaylist(name, desc) },
                    onDeletePlaylist = { p -> viewModel.deletePlaylist(p) },
                    onTrackSelected = { track, list -> viewModel.playTrack(track, list) }
                )
                AuraDestination.FAVORITES -> FavoritesScreen(
                    favoriteTracks = favoriteTracks,
                    currentPlayingId = playbackState.currentTrack?.id,
                    onTrackSelected = { track, list -> viewModel.playTrack(track, list) },
                    onToggleFavorite = { track -> viewModel.toggleFavorite(track) }
                )
                AuraDestination.HISTORY -> HistoryScreen(
                    historyTracks = historyTracks,
                    currentPlayingId = playbackState.currentTrack?.id,
                    onTrackSelected = { track, list -> viewModel.playTrack(track, list) },
                    onClearHistory = { viewModel.clearHistory() }
                )
                AuraDestination.DOWNLOADS -> DownloadsScreen(
                    downloadedTracks = downloadedTracks,
                    currentPlayingId = playbackState.currentTrack?.id,
                    onTrackSelected = { track, list -> viewModel.playTrack(track, list) },
                    onRemoveDownload = { id -> viewModel.removeDownload(id) }
                )
                AuraDestination.SETTINGS -> SettingsScreen(
                    onScanLibrary = { viewModel.scanLocalMusic() },
                    onClearHistory = { viewModel.clearHistory() }
                )
            }

            // Full Screen Player Modal Sheet
            AnimatedVisibility(
                visible = uiState.isPlayerSheetVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val currentTrack = playbackState.currentTrack
                val isFav = favoriteTracks.any { it.id == currentTrack?.id }

                PlayerModalSheet(
                    playbackState = playbackState,
                    isFavorite = isFav,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSeek = { pos -> viewModel.seekTo(pos) },
                    onNext = { viewModel.next() },
                    onPrevious = { viewModel.previous() },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeatMode() },
                    onToggleFavorite = { t -> viewModel.toggleFavorite(t) },
                    onDownload = { t -> viewModel.downloadTrack(t) },
                    onAddToPlaylist = { t ->
                        val defaultPl = playlists.firstOrNull()
                        if (defaultPl != null) {
                            viewModel.addTrackToPlaylist(defaultPl, t)
                        } else {
                            viewModel.createPlaylist("My Universe", "Favorites and discoveries")
                            viewModel.showSnackbar("Created 'My Universe' playlist and added track")
                        }
                    },
                    onDismiss = { viewModel.closePlayerSheet() }
                )
            }
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color.Black,
    selectedTextColor = AuraCyan,
    indicatorColor = AuraCyan,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary
)
