package com.raaveinm.chirro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.raaveinm.chirro.domain.PlaybackService
import com.raaveinm.chirro.ui.MainScreen
import com.raaveinm.chirro.ui.theme.ChirroTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> if (!isGranted) { finish() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChirroTheme {
                MainScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }

    ///////////////////////////////////////////////
    // Permission check & service start
    ///////////////////////////////////////////////
    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()

        com.raaveinm.chirro.domain.checkPermission(
            activity = this,
            launcher = requestPermissionLauncher
        )

        val serviceIntent = Intent(this, PlaybackService::class.java)
        startService(serviceIntent)
    }

    ///////////////////////////////////////////////
    // Sacrificing service
    ///////////////////////////////////////////////
//    @OptIn(UnstableApi::class)
//    override fun onDestroy() {
//        val serviceIntent = Intent(this, PlaybackService::class.java)
//        stopService(serviceIntent)
//        Log.i("MainActivity", "Service stopped")
//        super.onDestroy()
//    }
}
