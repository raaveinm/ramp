package com.raaveinm.chirro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.raaveinm.chirro.data.DatabaseManager
import com.raaveinm.chirro.domain.PlayerService
import com.raaveinm.chirro.ui.fragments.PlayerScreen
import com.raaveinm.chirro.ui.theme.ChirroTheme
import kotlinx.coroutines.launch
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val databaseManager = DatabaseManager()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("MainActivity", "READ_MEDIA_AUDIO permission granted.")
                initializeAppData()
            } else {
                Log.w("MainActivity", "READ_MEDIA_AUDIO permission denied.")
                Toast.makeText(this, "Permission denied. Cannot load music.",
                    Toast.LENGTH_LONG).show()
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
                Log.w("MainActivity", "POST_NOTIFICATIONS permission denied.")
                Toast.makeText(this, "Notifications disabled. Playback controls might not appear.",
                    Toast.LENGTH_LONG).show()

                checkAudioPermissionAndInitialize()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED -> {
                checkAudioPermissionAndInitialize()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            ChirroTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlayerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }


    private fun checkAudioPermissionAndInitialize() {
        val audioPermission =
            Manifest.permission.READ_MEDIA_AUDIO

        when {
            ContextCompat.checkSelfPermission(this, audioPermission) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("MainActivity", "Audio permission ($audioPermission) already granted.")
                initializeAppData()
            }
            shouldShowRequestPermissionRationale(audioPermission) -> {
                Log.d("MainActivity", "Showing rationale for Audio permission.")
                // Show rationale UI before requesting again (important for user experience)
                // For simplicity here, we launch directly, but add a dialog in a real app.
                requestPermissionLauncher.launch(audioPermission)
            }
            else -> {
                Log.d("MainActivity", "Requesting Audio permission ($audioPermission).")
                requestPermissionLauncher.launch(audioPermission)
            }
        }
    }


    // This function now runs only *after* necessary permissions are granted
    @OptIn(UnstableApi::class)
    private fun initializeAppData() {
        Log.i("MainActivity", "Initializing App Data (DB & Service)...")
        // Use lifecycleScope for coroutines tied to the Activity's lifecycle
        lifecycleScope.launch(Dispatchers.IO) { // Use IO dispatcher for DB operations
            try {
                Log.d("MainActivity", "Populating database...")
                // Call database population (consider doing this less frequently)
                // Maybe check if DB is already populated?
                databaseManager.databaseManager(this@MainActivity) // Pass context
                Log.d("MainActivity", "Database population process initiated.")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing database", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error loading music library.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        try {
            Log.d("MainActivity", "Starting PlayerService...")
            Intent(this, PlayerService::class.java).also { intent ->
                startForegroundService(intent)
            }
            Log.i("MainActivity", "PlayerService started.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error starting PlayerService", e)
            Toast.makeText(this, "Error starting playback service.", Toast.LENGTH_SHORT).show()
        }

        if ( false) {
            setContent { ChirroTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        PlayerScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }

    }

    @Composable
    fun PermissionRequiredScreen() {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Audio Permission Required")
            Spacer(modifier = Modifier.height(8.dp))
            Text("This app needs permission to access your audio files to play music.")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

}

