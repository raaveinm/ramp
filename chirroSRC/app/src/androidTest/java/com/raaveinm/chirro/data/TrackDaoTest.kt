package com.raaveinm.chirro.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TrackDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: TrackDatabase
    private lateinit var trackDao: TrackDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TrackDatabase::class.java
        ).allowMainThreadQueries().build()

        trackDao = database.trackDao()
    }

    @After
    fun teardown() = database.close()

    // Sample test data
    private fun createSampleTrack(
        id: Int = 0,
        title: String = "Test Song",
        artist: String = "Test Artist",
        album: String = "Test Album",
        duration: Long = 180000L,
        uri: String = "/test/path/song.mp3",
        artUri: String = "default",
        isFavorite: Boolean = false,
        included: Boolean = true
    ) = TrackInfo(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        uri = uri,
        artUri = artUri,
        isFavorite = isFavorite,
        included = included
    )

    @Test
    fun insertTrack_insertsTrackSuccessfully() = runTest {
        // Given
        val track = createSampleTrack()
        // When
        trackDao.insertTrack(track)
        // Then
        val tracks = trackDao.getTracks().first()
        assertEquals(1, tracks.size)
        assertEquals("Test Song", tracks[0].title)
        assertEquals("Test Artist", tracks[0].artist)
    }

    @Test
    fun insertTrack_withConflict_ignoresSecondInsert() = runTest {
        // Given
        val track1 = createSampleTrack(id = 1, title = "Song 1")
        val track2 = createSampleTrack(id = 1, title = "Song 2") // Same ID

        // When
        trackDao.insertTrack(track1)
        trackDao.insertTrack(track2) // Should be ignored due to OnConflictStrategy.IGNORE

        // Then
        val tracks = trackDao.getTracks().first()
        assertEquals(1, tracks.size)
        assertEquals("Song 1", tracks[0].title) // Original track should remain
    }

    @Test
    fun getTracks_returnsOnlyIncludedTracks() = runTest {
        // Given
        val includedTrack = createSampleTrack(title = "Included Song", included = true)
        val excludedTrack = createSampleTrack(title = "Excluded Song", included = false)

        // When
        trackDao.insertTrack(includedTrack)
        trackDao.insertTrack(excludedTrack)

        // Then
        val tracks = trackDao.getTracks().first()
        assertEquals(1, tracks.size)
        assertEquals("Included Song", tracks[0].title)
    }

    @Test
    fun updateTrack_updatesExistingTrack() = runTest {
        // Given
        val originalTrack = createSampleTrack(title = "Original Title")
        trackDao.insertTrack(originalTrack)
        val tracksAfterInsert = trackDao.getTracks().first()
        val insertedTrack = tracksAfterInsert[0]

        // When
        val updatedTrack = insertedTrack.copy(
            title = "Updated Title",
            isFavorite = true
        )
        trackDao.updateTrack(updatedTrack)

        // Then
        val tracks = trackDao.getTracks().first()
        assertEquals(1, tracks.size)
        assertEquals("Updated Title", tracks[0].title)
        assertTrue(tracks[0].isFavorite)
    }

    @Test
    fun deleteTrack_removesSpecificTrack() = runTest {
        // Given
        val track1 = createSampleTrack(title = "Song 1")
        val track2 = createSampleTrack(title = "Song 2")
        trackDao.insertTrack(track1)
        trackDao.insertTrack(track2)

        val tracksBeforeDelete = trackDao.getTracks().first()
        assertEquals(2, tracksBeforeDelete.size)

        // When
        trackDao.deleteTrack(tracksBeforeDelete[0])

        // Then
        val tracksAfterDelete = trackDao.getTracks().first()
        assertEquals(1, tracksAfterDelete.size)
        assertEquals("Song 2", tracksAfterDelete[0].title)
    }

    @Test
    fun deleteAllTracks_removesAllTracks() = runTest {
        // Given
        trackDao.insertTrack(createSampleTrack(title = "Song 1"))
        trackDao.insertTrack(createSampleTrack(title = "Song 2"))
        trackDao.insertTrack(createSampleTrack(title = "Song 3"))

        val tracksBeforeDelete = trackDao.getTracks().first()
        assertEquals(3, tracksBeforeDelete.size)

        // When
        trackDao.deleteAllTracks()

        // Then
        val tracksAfterDelete = trackDao.getTracks().first()
        assertEquals(0, tracksAfterDelete.size)
    }

    @Test
    fun getTrackById_returnsCorrectTrack() = runTest {
        // Given
        val track = createSampleTrack(title = "Specific Song")
        trackDao.insertTrack(track)
        val insertedTracks = trackDao.getTracks().first()
        val trackId = insertedTracks[0].id

        // When
        val retrievedTrack = trackDao.getTrackById(trackId)

        // Then
        assertEquals("Specific Song", retrievedTrack.title)
        assertEquals(trackId, retrievedTrack.id)
    }

    @Test
    fun getTrackByTitle_returnsCorrectTrack() = runTest {
        // Given
        val track = createSampleTrack(title = "Unique Title")
        trackDao.insertTrack(track)

        // When
        val retrievedTrack = trackDao.getTrackByTitle("Unique Title").first()

        // Then
        assertEquals("Unique Title", retrievedTrack.title)
    }

    @Test
    fun getTrackByArtist_returnsTracksFromArtist() = runTest {
        // Given
        val track1 = createSampleTrack(title = "Song 1", artist = "Artist A")
        val track2 = createSampleTrack(title = "Song 2", artist = "Artist A")
        val track3 = createSampleTrack(title = "Song 3", artist = "Artist B")

        trackDao.insertTrack(track1)
        trackDao.insertTrack(track2)
        trackDao.insertTrack(track3)

        // When
        val artistATracks = trackDao.getTrackByArtist("Artist A").first()

        // Then
        assertEquals(2, artistATracks.size)
        assertTrue(artistATracks.all { it.artist == "Artist A" })
    }

    @Test
    fun getTrackByAlbum_returnsTracksFromAlbum() = runTest {
        // Given
        val track1 = createSampleTrack(title = "Song 1", album = "Album X")
        val track2 = createSampleTrack(title = "Song 2", album = "Album X")
        val track3 = createSampleTrack(title = "Song 3", album = "Album Y")

        trackDao.insertTrack(track1)
        trackDao.insertTrack(track2)
        trackDao.insertTrack(track3)

        // When
        val albumXTracks = trackDao.getTrackByAlbum("Album X").first()

        // Then
        assertEquals(2, albumXTracks.size)
        assertTrue(albumXTracks.all { it.album == "Album X" })
    }

    @Test
    fun getFavouriteTracks_returnsOnlyFavorites() = runTest {
        // Given
        val favoriteTrack1 = createSampleTrack(title = "Favorite 1", isFavorite = true)
        val favoriteTrack2 = createSampleTrack(title = "Favorite 2", isFavorite = true)
        val regularTrack = createSampleTrack(title = "Regular Song", isFavorite = false)

        trackDao.insertTrack(favoriteTrack1)
        trackDao.insertTrack(favoriteTrack2)
        trackDao.insertTrack(regularTrack)

        // When
        val favoriteTracks = trackDao.getFavouriteTracks().first()

        // Then
        assertEquals(2, favoriteTracks.size)
        assertTrue(favoriteTracks.all { it.isFavorite })
    }
}