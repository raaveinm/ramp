package com.raaveinm.chirro.domain

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.*
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import androidx.media3.ui.PlayerNotificationManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.raaveinm.chirro.MainActivity
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.DatabaseManager
import com.raaveinm.chirro.domain.managment.QueueManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class PlayerService : MediaSessionService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var queueManager: QueueManager
    private lateinit var databaseManager: DatabaseManager

    companion object {
        const val CHANNEL_ID = "PlayerServiceChannel"
        const val NOTIFICATION_ID = 123
    }

    private val REFRESH_QUEUE_COMMAND = "com.raaveinm.chirro.REFRESH_QUEUE"
    private inner class PlayerSessionCallback : MediaSession.Callback {
        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            Log.d("PlayerService", "Controller connected: ${controller.packageName}")
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
                .add(SessionCommand(REFRESH_QUEUE_COMMAND, Bundle.EMPTY))
                .build()

            return MediaSession.ConnectionResult.accept(
                availableSessionCommands,
                connectionResult.availablePlayerCommands
            )
        }


        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == REFRESH_QUEUE_COMMAND) {
                serviceScope.launch { loadAndSetQueue() }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.map { it.buildUpon().setUri(it.requestMetadata.mediaUri).build() }.toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
        }

        @Deprecated("Deprecated in Java")
        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }

    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d("PlayerService", "onCreate called")
        createNotificationChannel()

        databaseManager = DatabaseManager()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true) // Pause when headphones unplugged
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(PlayerSessionCallback())
            .setId("ChirroMediaSession")
            .setSessionActivity(createSessionActivityPendingIntent())
            .build()

        serviceScope.launch {
            DatabaseManager()
            loadAndSetQueue()
        }

        setupNotification()
        player.addListener(PlayerListener())
        Log.d("PlayerService", "Service created, player and session initialized.")

    }

    private suspend fun loadAndSetQueue(startPlayback: Boolean = false, startIndex: Int = 0) {
        queueManager.prepareQueue()
        val mediaItems = queueManager.getMediaItems()

        if (mediaItems.isNotEmpty()) {
            player.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
            player.prepare()
            if (startPlayback) {
                player.play()
            }
        }
    }


    @SuppressLint("ForegroundServiceType")
    @OptIn(UnstableApi::class)
    private fun setupNotification() {
        val notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setChannelNameResourceId(R.string.channel_name)
            .setChannelDescriptionResourceId(R.string.channel_description)
            .build()

        notificationManager.setPlayer(player)
        // Optional: Customize notification further if needed
        // notificationManager.setUseFastForwardAction(false)
        // notificationManager.setUseRewindAction(false)

        startForeground(NOTIFICATION_ID, createInitialNotification().build())
    }

    // Create a basic notification for startForeground before the manager takes over
    private fun createInitialNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_initial_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
    }


    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = getString(R.string.channel_description)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createSessionActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    // --- Player Listener ---
    private inner class PlayerListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {  }
                Player.STATE_BUFFERING -> {  }
                Player.STATE_READY -> {  }
                Player.STATE_ENDED -> {  }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {}
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {}

        override fun onPlayerError(error: PlaybackException) = stopSelf()
    }
}
