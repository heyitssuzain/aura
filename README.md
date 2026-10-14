# AURA — YOUR MUSIC. YOUR UNIVERSE.

AURA is a premium Android music player featuring local audio playback, online music discovery, personal playlists, authorized downloads, and voice commands, all wrapped in a cinematic anime-inspired dark celestial interface.

## ✨ Core Highlights
- **100% Authentication-Free:** Launch straight into music without signup, OAuth, or accounts.
- **Local Audio Playback:** Scans device audio via Android MediaStore with instant library indexing.
- **AndroidX Media3 / ExoPlayer:** Rock-solid audio engine with foreground service background playback, lock-screen notification controls, and audio focus handling.
- **Room SQLite Database:** Stores custom playlists, track favorites, listening history, and offline download records completely on-device.
- **Voice Commands & Voice Search:** Hands-free playback control ("play", "pause", "next", "previous", "favorite", "search ...") with instant text alternative.
- **Cinematic Anime Aesthetic:** Sleek cosmic dark theme (`#0A0B10`), glowing cyan and violet aura accents, audio visualizer waves, and smooth micro-interactions.
- **Authorized Downloads:** Manage offline downloads with real file sizes and progress indicators.

## 🛠️ Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Audio Engine:** AndroidX Media3 ExoPlayer 1.5.1
- **Background Media:** AndroidX Media3 MediaSessionService
- **Persistence:** AndroidX Room Database 2.7.0 (with KSP)
- **Image Loading:** Coil Compose 2.7.0
- **Testing:** JUnit 4 + Robolectric local JVM testing

## 📦 Building the App

### Local Build
```bash
# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Build Debug APK
gradle :app:assembleDebug
```
The compiled APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

### GitHub Actions Build
The project is configured to automatically build and test on GitHub Actions for every push and pull request. To download the APK from GitHub Actions:
1. Go to the [Actions tab](https://github.com/heyitssuzain/aura/actions) in this repository
2. Select the latest workflow run
3. Scroll down to the "Artifacts" section
4. Download the `aura-music-player-debug-apk` artifact
5. Extract the ZIP file to get the `app-debug.apk`

**Note:** The APK downloaded from GitHub Actions is a debug build, signed with a debug key. It is suitable for testing and development but not for production distribution.

## 📜 Licenses
Licensed under the Apache License, Version 2.0. See `PRIVACY_AND_LICENSES.md` for complete privacy practices and third-party notices.
