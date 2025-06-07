package com.raaveinm.chirro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.raaveinm.chirro.domain.managment.AppDataInitializer
import com.raaveinm.chirro.ui.MainScreen
import com.raaveinm.chirro.ui.components.PermissionRequiredScreen
import com.raaveinm.chirro.ui.theme.ChirroTheme
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean ->
            if (isGranted) { initializeAppData() }
            else {
                Toast.makeText(
                    this,
                    "Permission denied. Cannot load music.",
                    Toast.LENGTH_LONG
                ).show()
                setContent {
                    ChirroTheme {
                        Surface(modifier = Modifier.fillMaxSize()) { PermissionRequiredScreen() }
                    }
                }
            }
        }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                checkAudioPermissionAndInitialize()
            } else {
                Toast.makeText(
                    this,
                    "Notifications disabled. Playback controls might not appear.",
                    Toast.LENGTH_LONG).show()

                checkAudioPermissionAndInitialize()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED -> { checkAudioPermissionAndInitialize() }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent { ChirroTheme { MainScreen() } }
    }

    private fun checkAudioPermissionAndInitialize() {
        val audioPermission = Manifest.permission.READ_MEDIA_AUDIO

        when {
            ContextCompat.checkSelfPermission(this, audioPermission) ==
                    PackageManager.PERMISSION_GRANTED -> { initializeAppData() }
            shouldShowRequestPermissionRationale(audioPermission) -> {
               requestPermissionLauncher.launch(audioPermission)
            }
            else -> { requestPermissionLauncher.launch(audioPermission) }
        }
    }

    @OptIn(UnstableApi::class)
    private fun initializeAppData() {
        lifecycleScope.launch(Dispatchers.IO) {
            AppDataInitializer().initializeAppData(this@MainActivity)
        }; AppDataInitializer().initializeService(this@MainActivity)
        if (false) setContent { ChirroTheme { MainScreen() } }
    }
}
