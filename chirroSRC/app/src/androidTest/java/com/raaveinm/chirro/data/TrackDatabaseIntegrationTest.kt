package com.raaveinm.chirro.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TrackDatabaseIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: TrackDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TrackDatabase::class.java
        ).build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun databaseCreation_createsSuccessfully() {
        assertNotNull(database)
        assertNotNull(database.trackDao())
    }

    @Test
    fun databaseOperations_workEndToEnd() = runTest {
        val dao = database.trackDao()

        // Insert
        val track = TrackInfo(
            title = "Integration Test Song",
            artist = "Test Artist",
            album = "Test Album",
            duration = 240000L,
            uri = "/test/path.mp3",
            artUri = "default",
            isFavorite = false,
            included = true
        )

        dao.insertTrack(track)

        // Retrieve
        val tracks = dao.getTracks().first()
        assertEquals(1, tracks.size)

        // Update
        val updatedTrack = tracks[0].copy(isFavorite = true)
        dao.updateTrack(updatedTrack)

        val favoriteTracks = dao.getFavouriteTracks().first()
        assertEquals(1, favoriteTracks.size)
        assertTrue(favoriteTracks[0].isFavorite)

        // Delete
        dao.deleteAllTracks()
        val emptyTracks = dao.getTracks().first()
        assertEquals(0, emptyTracks.size)
    }
}