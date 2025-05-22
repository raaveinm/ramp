package com.raaveinm.chirro.domain.managment

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.raaveinm.chirro.data.DatabaseManager
import com.raaveinm.chirro.domain.PlayerService

class InitDB {

    private val databaseManager = DatabaseManager()

    @UnstableApi
    fun initializeAppData(context: Context) {
        try { databaseManager.databaseManager(context) }
        catch (e: Exception) {
            Log.e("MainActivity", "Error initializing database", e)
        }
    }

    @OptIn(UnstableApi::class)
    fun initializeService (context: Context) {
        try {
            Intent(context, PlayerService::class.java).also { intent ->
                ContextCompat.startForegroundService(context, intent)
            }
        } catch (e: Exception) { Log.e("init", "Error initializing service", e) }
    }
}