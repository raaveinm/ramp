package com.raaveinm.chirro.data

import android.net.Uri
import androidx.room.TypeConverter
import androidx.core.net.toUri

class Converter {
    fun converter(){}
    @TypeConverter fun fromString(value: String?): Uri? = value?.toUri()
    @TypeConverter fun uriToString(uri: Uri?): String? = uri?.toString()
}