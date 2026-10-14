# AURA Android — Privacy & Third-Party Licenses

## Privacy Policy & Data Handling
- **No Mandatory Accounts:** AURA does not require user accounts, signup, or social login.
- **Zero Cloud Tracking:** All user library data, playlists, favorites, and play history are stored strictly device-local in an offline SQLite Room database.
- **Local Audio Privacy:** User local audio files scanned via MediaStore are never uploaded or transmitted off the device.
- **Permissions Usage:**
  - `READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE`: Needed exclusively to discover and play local music stored on the device.
  - `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: Required to keep audio playback running in the background and show system media notifications.
  - `POST_NOTIFICATIONS`: Displays Android MediaSession playback controls on lock screen and notification shade.
  - `RECORD_AUDIO`: Optional runtime permission used strictly on demand when user initiates Voice Commands or Voice Search. Audio data is processed via Android's SpeechRecognizer and never saved.
  - `INTERNET`: Used exclusively to stream permitted open online music tracks and stream artwork.

## Third-Party Open Source Licenses
- **AndroidX & Jetpack Compose:** Apache License 2.0 (Google LLC)
- **AndroidX Media3 (ExoPlayer & Session):** Apache License 2.0 (Google LLC)
- **Room Persistence Library:** Apache License 2.0 (Google LLC)
- **Coil Compose:** Apache License 2.0 (Coinverse / Coil Contributors)
- **Retrofit & Moshi:** Apache License 2.0 (Square, Inc.)
- **OkHttp:** Apache License 2.0 (Square, Inc.)
- **Kotlin Coroutines:** Apache License 2.0 (JetBrains s.r.o.)
