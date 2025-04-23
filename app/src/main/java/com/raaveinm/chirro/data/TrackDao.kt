package com.raaveinm.chirro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE) suspend fun insertTrack(track: TrackInfo)
    @Update suspend fun updateTrack(track: TrackInfo)
    @Delete suspend fun deleteTrack(track: TrackInfo)

    @Query ("select * from trackList where included = 1") fun getTracks(): Flow<List<TrackInfo>>
    @Query("select * from trackList where title = :title") fun getTrackByTitle(title: String): Flow<TrackInfo>
    @Query("select * from tracklist where artist = :artist") fun getTrackByArtist(artist: String): Flow<List<TrackInfo>>
    @Query("select * from tracklist where album = :album") fun getTrackByAlbum(album: String): Flow<List<TrackInfo>>
    @Query("select * from trackList where isFavorite = 1") fun getFavouriteTracks(): Flow<List<TrackInfo>>
}