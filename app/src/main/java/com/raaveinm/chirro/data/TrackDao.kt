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
    @Insert (onConflict = OnConflictStrategy.IGNORE) suspend fun insertTrack(track: TrackInfo)
    @Update suspend fun updateTrack(track: TrackInfo)
    @Delete suspend fun deleteTrack(track: TrackInfo)
    @Query("select * from TrackInfo where id = :id") suspend fun getTrackById(id: Int): TrackInfo
    @Query ("select * from TrackInfo") fun getAllTracks(): Flow<List<TrackInfo>>
}