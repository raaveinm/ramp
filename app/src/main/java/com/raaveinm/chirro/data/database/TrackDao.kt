package com.raaveinm.chirro.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Insert (onConflict = OnConflictStrategy.IGNORE) suspend fun insertTrack(track: FavTrackInfo)
    @Delete suspend fun deleteTrack(track: FavTrackInfo)
    @Query("SELECT id FROM FavTrackInfo") fun getFavoriteIds(): Flow<List<Long>>
    @Query("SELECT EXISTS(SELECT * FROM FavTrackInfo WHERE id = :id)")
    suspend fun isTrackFavorite(id: Long): Boolean
}