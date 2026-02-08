package com.raaveinm.chirro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavTrackInfo::class], version = 2, exportSchema = false)
internal abstract class TrackDatabase: RoomDatabase() {
    abstract fun trackDao(): TrackDao
    companion object{
        @Volatile private var instance: TrackDatabase? = null
        fun getDatabase(context: Context): TrackDatabase = instance ?: synchronized(this){
            Room.databaseBuilder(context, TrackDatabase::class.java, "track_database")
                .fallbackToDestructiveMigration(true)
                .build().also { instance = it }
        }
    }
}