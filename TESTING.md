# AURA Android — Testing Guide

## Automated Testing
The project uses JUnit 4 and Robolectric for fast local JVM testing of Critical User Journeys (CUJs), database operations, and viewmodel business logic without requiring an emulator.

### Running Unit & Robolectric Tests
Execute the following Gradle command in the root project:
```bash
gradle :app:testDebugUnitTest
```

### Test Coverage Highlights
1. **AuraDatabaseTest:**
   - Tests insertion and retrieval of Tracks, Favorites, Playlists, and Play History.
   - Verifies cascading and conflict replacement rules.
2. **AuraMainViewModelTest:**
   - Verifies authentication-free launch and intro-completion state storage.
   - Verifies queue addition, play/pause state transitions, and search query flows.
   - Verifies voice command parsing logic ("play", "pause", "next", "previous", "favorite").

## Manual Verification Scenarios
1. **First Launch:**
   - Launch app -> Anime landing page renders with "YOUR MUSIC. YOUR UNIVERSE.".
   - Click "EXPLORE FEATURES" -> Navigates/scrolls to feature summary.
   - Click "ENTER AURA" -> Enters music player directly without any login prompt.
2. **Local Music Scanning:**
   - Grant Audio permission -> Reads audio files from device MediaStore.
   - Displays real track title, artist, duration, and album art fallback.
3. **Audio Playback:**
   - Tap any track -> Starts playback via Media3 ExoPlayer.
   - Mini player updates at bottom -> Tap mini player opens full-screen player modal.
   - Seekbar scrubs playback, toggle play/pause, next/previous updates queue.
4. **Favorites & Playlists:**
   - Tap heart icon -> Track saved to Room favorites.
   - Create playlist -> Add tracks -> Persists across app cold starts.
5. **Voice Commands:**
   - Tap voice search microphone -> Speak "play" or "favorite" -> Triggers real action or search query.
