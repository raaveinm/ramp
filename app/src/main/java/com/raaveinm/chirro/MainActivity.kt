package com.raaveinm.chirro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.raaveinm.chirro.domain.PlaybackService
import com.raaveinm.chirro.ui.MainScreen
import com.raaveinm.chirro.ui.features.PermissionHandler
import com.raaveinm.chirro.ui.screens.PermissionDeniedScreen
import com.raaveinm.chirro.ui.theme.ChirroTheme
import com.raaveinm.chirro.ui.veiwmodel.AppViewModelProvider
import com.raaveinm.chirro.ui.veiwmodel.SettingsViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var permissionHandler: PermissionHandler
    private var permissionGranted by mutableStateOf(false)
    private var isPermanentlyDenied by mutableStateOf(false)

    private val audioPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        permissionHandler = PermissionHandler(this) { results ->
            val granted = (results[audioPermission] == true) || checkAudioPermission()
            permissionGranted = granted

            if (!granted) {
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    audioPermission
                )
                isPermanentlyDenied = !showRationale
            }
        }

        checkAndRequestPermissions()

        val serviceIntent = Intent(this, PlaybackService::class.java)
        startService(serviceIntent)

        setContent {
            val viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val uiState by viewModel.uiState.collectAsState()
            ChirroTheme(
                appTheme = uiState.currentTheme
            ) {
                if (!permissionGranted) {
                    PermissionDeniedScreen(
                        isPermanentlyDenied = isPermanentlyDenied,
                        onRequestPermission = {
                            permissionHandler.requestPermissions(
                                this@MainActivity,
                                listOf(audioPermission)
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MainScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionState()
    }

    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            audioPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun updatePermissionState() {
        val granted = checkAudioPermission()
        permissionGranted = granted
        if (!granted) {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                audioPermission
            )
            isPermanentlyDenied = !showRationale
        }
    }

    private fun checkAndRequestPermissions() {
        if (checkAudioPermission()) {
            permissionGranted = true
        } else {
            permissionHandler.requestPermissions(
                this,
                listOf(audioPermission)
            )
        }
    }
}
