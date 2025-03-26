package com.raaveinm.ramp.services

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.raaveinm.ramp.R
import com.raaveinm.ramp.MainActivity
import com.raaveinm.ramp.snippets.AudioItem
import com.raaveinm.ramp.snippets.ContentResolver
import kotlinx.coroutines.*


private const val TAG = "RampLibraryService"
const val SAVE_TO_FAVORITES = "SAVE_TO_FAVORITES"

class ChirroPlayer : MediaLibraryService() {

    private var mediaLibrarySession: MediaLibrarySession? = null
    private lateinit var player: ExoPlayer
    private lateinit var wrappedPlayer: Player // For error handling wrapper
    private lateinit var customCommands: List<CommandButton>

    // Coroutine scope for background tasks like loading media
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob()) // Use Main for player access, IO for file access

    // --- Service Lifecycle ---

    @OptIn(UnstableApi::class) // For ErrorHandling and AudioAttributes
    override fun onCreate() {
        super.onCreate()

        customCommands = createCustomCommands()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true) // Pause when headphones unplugged
            .build()

        // Optional: Add EventLogger for debugging
        player.addAnalyticsListener(EventLogger())

        // Wrap the player for custom error handling
        wrappedPlayer = ErrorHandling(this, player) // Use your ErrorHandling class

        // Create the MediaLibrarySession
        mediaLibrarySession = MediaLibrarySession.Builder(
            this,
            wrappedPlayer, // Use the wrapped player
            LibrarySessionCallback() // Use our custom callback
        )
            .setSessionActivity(createSessionActivityPendingIntent()) // Intent to launch UI
            .build()

        Log.d(TAG, "Service Created and Session Initialized")

        // Load media items when the service starts (or on demand)
        loadLocalAudioFiles()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        Log.d(TAG, "Releasing MediaSession and Player")
        mediaLibrarySession?.let { session ->
            session.run {
                val actualPlayer = (session.player as? ErrorHandling)?.wrappedPlayer ?: session.player
                actualPlayer.release()
                release()
            }
        }
        mediaLibrarySession = null
        serviceScope.cancel()
        if (::player.isInitialized) {
        }
        super.onDestroy()
        Log.d(TAG, "Service Destroyed")
    }

    // --- Media Loading ---

    private fun loadLocalAudioFiles() {
        serviceScope.launch {
            Log.d(TAG, "Starting to load local audio files...")
            val audioItems = fetchAudioItemsFromDevice()
            if (audioItems.isNotEmpty()) {
                val mediaItems = audioItems.map { it.toMediaItem() }
                // Set items on the player using the Main dispatcher
                withContext(Dispatchers.Main) {
                    player.setMediaItems(mediaItems)
                    player.prepare() // Prepare the player after setting items
                    Log.d(TAG, "Loaded ${mediaItems.size} media items.")
                }
            } else {
                Log.d(TAG, "No audio items found on device.")
                // Handle empty library case if needed (e.g., update session metadata)
            }
        }
    }

    // Use your ContentResolver - Ensure it runs on a background thread
    private suspend fun fetchAudioItemsFromDevice(): List<AudioItem> {
        return withContext(Dispatchers.IO) { // Ensure ContentResolver runs off the main thread
            try {
                ContentResolver(applicationContext).getAudioData()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching audio data", e)
                emptyList<AudioItem>() // Return empty list on error
            }
        }
    }

    @OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun AudioItem.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(this.mediaId)
            .setUri(this.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(this.title.ifBlank { this.displayName })
                    .setArtist(this.artist.takeIf { it.isNotBlank() && it != "<unknown>" })
                    .setAlbumTitle(this.album)
                    .setArtworkUri(this.albumArtUri)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .build()
            )
            .build()
    }


    // --- Session Activity ---

    private fun createSessionActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java) // Intent to launch your MainActivity
        return PendingIntent.getActivity(
            this,
            0, // Request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Flags
        )
    }

    // --- Custom Commands ---

    private fun createCustomCommands(): List<CommandButton> {
        // Example: Favorite button
        return listOf(
            CommandButton.Builder()
                .setSessionCommand(SessionCommand(SAVE_TO_FAVORITES, Bundle.EMPTY))
                .setDisplayName("Favorite")
                .setIconResId(R.drawable.playlist_add_check)
                .setEnabled(true)
                .build()
        )
    }


    // --- MediaLibrarySession Callback ---

    @OptIn(UnstableApi::class)
    private inner class LibrarySessionCallback : MediaLibrarySession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val availableSessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
            customCommands.forEach { commandButton ->
                commandButton.sessionCommand?.let { availableSessionCommands.add(it) }
            }

            Log.d(TAG, "Controller connected: ${controller.packageName}")

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(availableSessionCommands.build()) //Modified this line
                .setCustomLayout(ImmutableList.copyOf(customCommands))
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            Log.d(TAG, "Received custom command: ${customCommand.customAction}")
            when (customCommand.customAction) {
                SAVE_TO_FAVORITES -> {
                    val currentItem = player.currentMediaItem
                    if (currentItem != null) {
                        Log.i(TAG, "Saving ${currentItem.mediaMetadata.title} to favorites (placeholder)")
                    }
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            }
            // If command is not recognized
            return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
        }
    }
}