package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUri: String,
    val artworkUrl: String?,
    val isLocal: Boolean,
    val isStreamable: Boolean,
    val isDownloaded: Boolean,
    val sourceProvider: String,
    val bitRateKbps: Int
) {
    fun toDomain(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        mediaUri = mediaUri,
        artworkUrl = artworkUrl,
        isLocal = isLocal,
        isStreamable = isStreamable,
        isDownloaded = isDownloaded,
        sourceProvider = sourceProvider,
        bitRateKbps = bitRateKbps
    )

    companion object {
        fun fromDomain(track: Track): TrackEntity = TrackEntity(
            id = track.id,
            title = track.title,
            artist = track.artist,
            album = track.album,
            durationMs = track.durationMs,
            mediaUri = track.mediaUri,
            artworkUrl = track.artworkUrl,
            isLocal = track.isLocal,
            isStreamable = track.isStreamable,
            isDownloaded = track.isDownloaded,
            sourceProvider = track.sourceProvider,
            bitRateKbps = track.bitRateKbps
        )
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_items")
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val trackId: String,
    val position: Int
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val trackId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "play_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: String,
    val playedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val trackId: String,
    val localFilePath: String,
    val bytesTotal: Long,
    val bytesDownloaded: Long,
    val status: String,
    val downloadedAt: Long = System.currentTimeMillis()
)
