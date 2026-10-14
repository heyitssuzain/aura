package com.example.data.repository

import com.example.data.local.AuraDatabase
import com.example.data.local.DownloadEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistItemEntity
import com.example.data.local.TrackEntity
import com.example.data.remote.OnlineMusicProvider
import com.example.data.scanner.LocalAudioScanner
import com.example.model.Playlist
import com.example.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuraRepository(
    private val database: AuraDatabase,
    private val localScanner: LocalAudioScanner,
    private val onlineProvider: OnlineMusicProvider
) {
    private val trackDao = database.trackDao()
    private val playlistDao = database.playlistDao()
    private val favoriteDao = database.favoriteDao()
    private val historyDao = database.historyDao()
    private val downloadDao = database.downloadDao()

    // Tracks
    val localTracksFlow: Flow<List<Track>> =
        trackDao.getLocalTracks().map { list -> list.map { it.toDomain() } }

    val allSavedTracksFlow: Flow<List<Track>> =
        trackDao.getAllTracks().map { list -> list.map { it.toDomain() } }

    val favoriteTracksFlow: Flow<List<Track>> =
        favoriteDao.getFavoriteTracks().map { list -> list.map { it.toDomain() } }

    val historyTracksFlow: Flow<List<Track>> =
        historyDao.getRecentHistoryTracks().map { list -> list.map { it.toDomain() } }

    val downloadedTracksFlow: Flow<List<Track>> =
        downloadDao.getDownloadedTracks().map { list -> list.map { it.toDomain() } }

    val playlistsFlow: Flow<List<Playlist>> =
        playlistDao.getAllPlaylists().map { list ->
            list.map { entity ->
                Playlist(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    createdAt = entity.createdAt
                )
            }
        }

    fun isFavorite(trackId: String): Flow<Boolean> = favoriteDao.isFavorite(trackId)

    suspend fun toggleFavorite(track: Track): Boolean {
        saveTrack(track)
        val currentlyFav = favoriteDao.isFavoriteDirect(track.id)
        if (currentlyFav) {
            favoriteDao.removeFavorite(track.id)
            return false
        } else {
            favoriteDao.addFavorite(FavoriteEntity(trackId = track.id))
            return true
        }
    }

    suspend fun saveTrack(track: Track) {
        trackDao.insertTrack(TrackEntity.fromDomain(track))
    }

    suspend fun scanAndSyncLocalTracks(): List<Track> {
        val scanned = localScanner.scanLocalAudio()
        if (scanned.isNotEmpty()) {
            trackDao.insertTracks(scanned.map { TrackEntity.fromDomain(it) })
        }
        return scanned
    }

    suspend fun getFeaturedOnlineTracks(): List<Track> {
        val online = onlineProvider.getFeaturedTracks()
        // Cache in tracks table so foreign keys in playlists and history remain intact
        trackDao.insertTracks(online.map { TrackEntity.fromDomain(it) })
        return online
    }

    suspend fun searchOnline(query: String): Result<List<Track>> {
        val result = onlineProvider.searchOnlineTracks(query)
        result.onSuccess { list ->
            if (list.isNotEmpty()) {
                trackDao.insertTracks(list.map { TrackEntity.fromDomain(it) })
            }
        }
        return result
    }

    suspend fun resolveAudioStream(track: Track): String {
        return onlineProvider.resolveAudioStream(track)
    }

    suspend fun recordHistory(track: Track) {
        saveTrack(track)
        historyDao.recordHistory(HistoryEntity(trackId = track.id))
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    // Playlists
    suspend fun createPlaylist(title: String, description: String = ""): Long {
        return playlistDao.createPlaylist(PlaylistEntity(title = title, description = description))
    }

    suspend fun updatePlaylist(id: Long, title: String, description: String) {
        playlistDao.updatePlaylist(id, title, description)
    }

    suspend fun deletePlaylist(id: Long) {
        playlistDao.deletePlaylistItems(id)
        playlistDao.deletePlaylist(id)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        saveTrack(track)
        playlistDao.addTrackToPlaylist(
            PlaylistItemEntity(
                playlistId = playlistId,
                trackId = track.id,
                position = 0
            )
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> =
        playlistDao.getTracksForPlaylist(playlistId).map { list -> list.map { it.toDomain() } }

    // Downloads
    suspend fun recordDownload(track: Track, filePath: String, sizeBytes: Long) {
        saveTrack(track.copy(isDownloaded = true))
        downloadDao.insertDownload(
            DownloadEntity(
                trackId = track.id,
                localFilePath = filePath,
                bytesTotal = sizeBytes,
                bytesDownloaded = sizeBytes,
                status = "COMPLETED"
            )
        )
    }

    suspend fun removeDownload(trackId: String) {
        downloadDao.deleteDownload(trackId)
    }
}
