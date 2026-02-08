package com.raaveinm.chirro

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.raaveinm.chirro.domain.PlaybackService
import com.raaveinm.chirro.ui.MainScreen
import com.raaveinm.chirro.ui.theme.ChirroTheme
import com.raaveinm.chirro.ui.veiwmodel.AppViewModelProvider
import com.raaveinm.chirro.ui.veiwmodel.SettingsViewModel

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> if (!isGranted) { finish() } }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, PlaybackService::class.java)
        startService(serviceIntent)

        enableEdgeToEdge()
        setContent {
            val viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val uiState by viewModel.uiState.collectAsState()
            ChirroTheme(
                appTheme = uiState.currentTheme
            ) {

                MainScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }

    ///////////////////////////////////////////////
    // Permission check
    ///////////////////////////////////////////////
    override fun onStart() {
        super.onStart()
        com.raaveinm.chirro.domain.checkPermission(
            activity = this,
            launcher = requestPermissionLauncher
        )
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
