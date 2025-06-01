package com.raaveinm.chirro.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class DatabaseManagerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var database: TrackDatabase
    private lateinit var databaseManager: DatabaseManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, TrackDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        databaseManager = DatabaseManager()

        mockkObject(TrackDatabase.Companion)
        every { TrackDatabase.getDatabase(any()) } returns database
    }

    @After
    fun teardown() {
        database.close()
        unmockkAll()
    }

    private fun createSampleTrack(
        title: String = "Test Song",
        artist: String = "Test Artist"
    ) = TrackInfo(
        title = title,
        artist = artist,
        album = "Test Album",
        duration = 180000L,
        uri = "/test/path/song.mp3",
        artUri = "default",
        isFavorite = false,
        included = true
    )

    @Test
    fun getInitialTrackList_returnsCorrectList() = runTest {
        // Given
        val sampleTracks = listOf(
            createSampleTrack("Song 1"),
            createSampleTrack("Song 2")
        )

        sampleTracks.forEach { database.trackDao().insertTrack(it) }

        // When
        val result = databaseManager.getInitialTrackList(context)

        // Then
        assertEquals(2, result.size)
        assertEquals("Song 1", result[0].title)
        assertEquals("Song 2", result[1].title)
    }

    @Test
    fun getDatabase_returnsFlow() = runTest {
        // Given
        val sampleTrack = createSampleTrack()
        database.trackDao().insertTrack(sampleTrack)

        // When
        val flow = databaseManager.getDatabase(context)
        val result = flow.first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Test Song", result[0].title)
    }

    @Test
    fun deleteDatabase_removesAllTracks() = runTest {
        // Given
        database.trackDao().insertTrack(createSampleTrack("Song 1"))
        database.trackDao().insertTrack(createSampleTrack("Song 2"))

        val tracksBeforeDelete = database.trackDao().getTracks().first()
        assertEquals(2, tracksBeforeDelete.size)

        // When
        databaseManager.deleteDatabase(context)

        // Then
        val tracksAfterDelete = database.trackDao().getTracks().first()
        assertEquals(0, tracksAfterDelete.size)
    }

    @Test
    fun getTrackById_returnsCorrectTrack() = runTest {
        // Given
        val track = createSampleTrack("Specific Song")
        database.trackDao().insertTrack(track)
        val insertedTracks = database.trackDao().getTracks().first()
        val trackId = insertedTracks[0].id

        // When
        val result = databaseManager.getTrackById(context, trackId)

        // Then
        assertEquals("Specific Song", result.title)
        assertEquals(trackId, result.id)
    }

    @Test
    fun updateTrackFavoriteStatus_updatesCorrectly() = runTest {
        // Given
        val track = createSampleTrack("Test Song")
        database.trackDao().insertTrack(track)
        val insertedTracks = database.trackDao().getTracks().first()
        val trackId = insertedTracks[0].id
        assertFalse(insertedTracks[0].isFavorite)
        // When
        databaseManager.updateTrackFavoriteStatus(context, trackId, true)
        // Then
        val updatedTrack = database.trackDao().getTrackById(trackId)
        assertTrue(updatedTrack.isFavorite)
    }

}