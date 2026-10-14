package com.example.model

data class Playlist(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val trackCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class PlaylistItem(
    val id: Long = 0,
    val playlistId: Long,
    val trackId: String,
    val position: Int = 0
)

data class FavoriteRecord(
    val trackId: String,
    val addedAt: Long = System.currentTimeMillis()
)

data class HistoryRecord(
    val id: Long = 0,
    val trackId: String,
    val playedAt: Long = System.currentTimeMillis()
)

data class DownloadRecord(
    val trackId: String,
    val localFilePath: String,
    val bytesTotal: Long,
    val bytesDownloaded: Long,
    val status: DownloadStatus = DownloadStatus.COMPLETED,
    val downloadedAt: Long = System.currentTimeMillis()
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED
}
