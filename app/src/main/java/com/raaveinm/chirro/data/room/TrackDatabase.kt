package com.raaveinm.chirro.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database (entities = [TrackInfo::class], version = 1, exportSchema = false)
abstract class TrackDatabase: RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object{
        @Volatile private var instance: TrackDatabase? = null

        fun getDatabase(context: Context): TrackDatabase = instance ?: synchronized(this){
            Room.databaseBuilder(context, TrackDatabase::class.java, "track_database")
                .build().also { instance = it }
        }
    }
}