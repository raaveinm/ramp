package com.raaveinm.chirro.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavTrackInfo(
    @PrimaryKey(autoGenerate = false) val id: Int,
    @ColumnInfo val uri: String,
)