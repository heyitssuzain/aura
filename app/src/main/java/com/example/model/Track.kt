package com.example.model

data class Track(
    val id: String,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Aura Universe",
    val durationMs: Long = 0L,
    val mediaUri: String = "",
    val artworkUrl: String? = null,
    val isLocal: Boolean = false,
    val isStreamable: Boolean = true,
    val isDownloaded: Boolean = false,
    val sourceProvider: String = "AURA_CORE",
    val bitRateKbps: Int = 320
) {
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "--:--"
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
