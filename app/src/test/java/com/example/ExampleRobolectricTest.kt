package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AuraDatabase
import com.example.data.local.AuraPreferences
import com.example.data.local.FavoriteEntity
import com.example.data.local.TrackEntity
import com.example.model.Track
import com.example.model.VoiceCommandResult
import com.example.voice.AuraVoiceManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AuraDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AuraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun read_appName_from_context() {
        val appName = context.getString(R.string.app_name)
        assertEquals("AURA", appName)
    }

    @Test
    fun database_insertAndQueryTrackAndFavorite() = runBlocking {
        val track = Track(
            id = "test_01",
            title = "Test Starlight",
            artist = "Aura Artist",
            album = "Test Album",
            durationMs = 180000L,
            mediaUri = "https://example.com/audio.mp3"
        )

        database.trackDao().insertTrack(TrackEntity.fromDomain(track))
        val fetched = database.trackDao().getTrackById("test_01")
        assertEquals("Test Starlight", fetched?.title)

        database.favoriteDao().addFavorite(FavoriteEntity(trackId = "test_01"))
        val isFav = database.favoriteDao().isFavoriteDirect("test_01")
        assertTrue(isFav)

        database.favoriteDao().removeFavorite("test_01")
        val isFavAfter = database.favoriteDao().isFavoriteDirect("test_01")
        assertFalse(isFavAfter)
    }

    @Test
    fun voiceCommand_parser_accuracy() {
        val voiceManager = AuraVoiceManager(context)

        assertEquals(VoiceCommandResult.Play, voiceManager.processVoiceCommand("play"))
        assertEquals(VoiceCommandResult.Pause, voiceManager.processVoiceCommand("pause"))
        assertEquals(VoiceCommandResult.NextTrack, voiceManager.processVoiceCommand("next"))
        assertEquals(VoiceCommandResult.PreviousTrack, voiceManager.processVoiceCommand("previous"))
        assertEquals(VoiceCommandResult.FavoriteCurrent, voiceManager.processVoiceCommand("favorite"))
        assertEquals(VoiceCommandResult.OpenLibrary, voiceManager.processVoiceCommand("open library"))

        val searchResult = voiceManager.processVoiceCommand("search starlight")
        assertTrue(searchResult is VoiceCommandResult.SearchQuery)
        assertEquals("starlight", (searchResult as VoiceCommandResult.SearchQuery).query)
    }

    @Test
    fun preferences_introCompletion_persistence() {
        val prefs = AuraPreferences(context)
        prefs.isIntroCompleted = false
        assertFalse(prefs.isIntroCompleted)

        prefs.isIntroCompleted = true
        assertTrue(prefs.isIntroCompleted)
    }

    @Test
    fun onlineMusicProvider_search_returnsResults() = runBlocking {
        val provider = com.example.data.remote.OnlineMusicProvider()
        val result = provider.searchOnlineTracks("Starlight")
        assertTrue(result.isSuccess)
        val tracks = result.getOrNull()
        assertTrue(tracks != null && tracks.isNotEmpty())
        assertTrue(tracks!!.any { it.title.contains("Starlight", ignoreCase = true) })
    }

    @Test
    fun mainViewModel_instantiation_succeeds() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.MainViewModel(app)
        assertFalse(viewModel.uiState.value.isIntroCompleted)
    }
}


