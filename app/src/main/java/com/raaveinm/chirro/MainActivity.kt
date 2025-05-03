package com.raaveinm.chirro

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.raaveinm.chirro.data.DatabaseManager
import com.raaveinm.chirro.domain.PlayerService
import com.raaveinm.chirro.ui.fragments.PlayerScreen
import com.raaveinm.chirro.ui.theme.ChirroTheme

/**
 * This project is designed as course work, but I fill it with my love and respect for Android and
 * Peoples who are inspiring and motivating to be the best version of myself
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChirroTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlayerScreen(modifier = Modifier.padding(innerPadding), context = this)
                }
            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            DatabaseManager().databaseManager(context = this)
        }
        Intent(this, PlayerService::class.java).also { startService(it) }
    }
}
