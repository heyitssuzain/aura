# AURA Android — Architecture Specification

## Architectural Overview
AURA follows the modern Android MVVM (Model-View-ViewModel) and Repository patterns with a strict separation of concerns:

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│  - Jetpack Compose M3 (AuraTheme)                      │
│  - LandingScreen, MainContainer, PlayerBottomSheet     │
│  - Screens: Home, Search, Library, Playlists,          │
│             Favorites, History, Downloads, Settings    │
└───────────────────────────┬────────────────────────────┘
                            │ (Events & StateFlow)
┌───────────────────────────▼────────────────────────────┐
│                    ViewModel Layer                     │
│  - AuraMainViewModel: Manages active navigation,       │
│    search state, voice commands, library scans, queue  │
└─────────────┬───────────────────────────┬──────────────┘
              │                           │
┌─────────────▼───────────────┐ ┌─────────▼──────────────┐
│       Data & Repositories   │ │     Playback Engine    │
│  - AuraRepository           │ │  - AuraPlayerManager   │
│  - AuraDatabase (Room)      │ │  - AuraPlaybackService │
│    - TrackDao, PlaylistDao  │ │    (Media3 & Session)  │
│    - HistoryDao, FavoriteDao│ │  - Audio Focus & State │
│  - LocalMediaStoreScanner   │ └────────────────────────┘
│  - OnlineDiscoveryProvider  │
│  - DownloadManagerHelper    │
└─────────────────────────────┘
```

## Layers and Responsibilities

1. **Presentation / UI Layer (`com.example.ui`):**
   - Built 100% in Jetpack Compose with Material 3.
   - Dark celestial anime visual identity: deep space charcoal/black backgrounds (`#0A0B10`), glowing cyan (`#00E5FF`), deep violet (`#8A2BE2`), and electric blue.
   - `LandingScreen`: First-launch onboarding with "ENTER AURA" and "EXPLORE FEATURES" options, completely auth-free.
   - Mini Player bar sticky at bottom, expandable into full-screen `PlayerModalSheet` with scrubber, playback controls, repeat/shuffle, and lyrics/visualizer glow.
   - Test tags added to interactive elements for UI testing.

2. **Playback Layer (`com.example.playback`):**
   - `AuraPlaybackService`: AndroidX Media3 `MediaSessionService` running in foreground with `mediaPlayback` type.
   - Handles background playback, Android MediaSession notification controls, headset disconnect (noisy intent), and audio focus.
   - `AuraPlayerManager`: Central singleton client that binds or connects to the player and exposes state (`isPlaying`, `currentTrack`, `currentPosition`, `duration`, `playbackState`, `queue`).

3. **Data Layer (`com.example.data`):**
   - **Room Database (`AuraDatabase`):**
     - `TrackEntity`: Stores local tracks, downloaded tracks, and cached stream metadata.
     - `PlaylistEntity` & `PlaylistItemEntity`: User-created custom playlists.
     - `FavoriteEntity`: Track favorites.
     - `HistoryEntity`: Playback history with timestamps.
     - `DownloadEntity`: Track downloads with file URIs, progress, and byte size.
   - **Local Scanning (`LocalAudioScanner`):**
     - Queries Android `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` safely off the main thread.
   - **Online Discovery (`OnlineDiscoveryProvider`):**
     - Provides free, legal, open music streams (Free Music Archive / Jamendo / CC music catalog) with normalized metadata, explicit distinction between streamable vs preview.

4. **Voice Processing (`com.example.voice`):**
   - `AuraVoiceController`: Wraps Android `SpeechRecognizer` with permission checks and parses natural commands ("play", "pause", "next", "previous", "favorite", "search [query]").
