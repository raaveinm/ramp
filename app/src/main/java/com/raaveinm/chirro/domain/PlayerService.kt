package com.raaveinm.chirro.domain

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import android.graphics.BitmapFactory
import android.Manifest
import android.net.Uri
import androidx.media3.ui.PlayerNotificationManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.raaveinm.chirro.MainActivity
import com.raaveinm.chirro.R
import com.raaveinm.chirro.data.room.DatabaseManager
import com.raaveinm.chirro.domain.managment.QueueManager
import com.raaveinm.chirro.domain.usecase.Commands
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Suppress("DEPRECATION")
@UnstableApi
class PlayerService : MediaSessionService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private val databaseManager: DatabaseManager by lazy { DatabaseManager() }
    private val queueManager: QueueManager by lazy { QueueManager(this, databaseManager) }
    private var playerNotificationManager: PlayerNotificationManager? = null

    private inner class PlayerSessionCallback : MediaSession.Callback {
        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            if (player == null) {
                Log.e("PlayerService", "Player not initialized during onConnect")
                return MediaSession.ConnectionResult.reject()
            }

            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
                .add(SessionCommand(Commands.ACTION_REFRESH_QUEUE, Bundle.EMPTY))
                .add(SessionCommand(Commands.ACTION_TOGGLE_FAVORITE, Bundle.EMPTY))
                .build()

            return MediaSession.ConnectionResult.accept(
                availableSessionCommands,
                connectionResult.availablePlayerCommands
            )
        }

        @SuppressLint("CheckResult")
        @OptIn(UnstableApi::class)
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                Commands.ACTION_REFRESH_QUEUE -> {
                    Log.d("PlayerService", "Received REFRESH_QUEUE command")
                    serviceScope.launch { loadAndSetQueue() }.invokeOnCompletion {
                        Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }
                Commands.ACTION_TOGGLE_FAVORITE -> {
                    val trackId = args.getInt(Commands.EXTRA_TRACK_ID, -1)
                    if (trackId != -1) {
                        serviceScope.launch { toggleFavoriteStatus(trackId) }
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    return Futures.immediateFuture(SessionResult(SessionError.ERROR_INVALID_STATE))
                }
                else -> {}
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }

        private suspend fun toggleFavoriteStatus(trackId: Int) {
            withContext(Dispatchers.IO) {
                try {
                    val currentTrack = databaseManager.getTrackById(this@PlayerService, trackId)
                    val newFavoriteStatus = !currentTrack.isFavorite
                    databaseManager.updateTrackFavoriteStatus(
                        this@PlayerService,
                        trackId,
                        newFavoriteStatus
                    )
                } catch (e: Exception) {
                    Log.e(
                        "PlayerService",
                        "Error updating favorite status for track $trackId", e
                    )
                }
            }
        }

        @OptIn(UnstableApi::class)
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.mapNotNull {
                if (it.requestMetadata.mediaUri != null) {
                    it.buildUpon().setUri(it.requestMetadata.mediaUri).build()
                } else { null }
            }.toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
        }
    }

    @SuppressLint("ForegroundServiceType")
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.e("PlayerService", "FOREGROUND_SERVICE permission not granted")
            return
        }

        super.onCreate()
        Log.d("PlayerService", "onCreate called")
        createNotificationChannel()

        player = ExoPlayer.Builder(this)
            .setRenderersFactory(DefaultRenderersFactory(this)
                .setExtensionRendererMode(DefaultRenderersFactory
                    .EXTENSION_RENDERER_MODE_PREFER))
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build().also {
                it.addListener(PlayerListener())
            }

        mediaSession = MediaSession.Builder(this, player!!)
            .setCallback(PlayerSessionCallback())
            .setId("ChirroMediaSession_${System.currentTimeMillis()}")
            .setSessionActivity(createSessionActivityPendingIntent())
            .build()

        setupMediaStyleNotification()

        serviceScope.launch {
            try {
                loadAndSetQueue()
            } catch (e: Exception) {
                Log.e("PlayerService", "Error during initial queue load", e)
            }
        }
    }


    @OptIn(UnstableApi::class)
    private suspend fun loadAndSetQueue(startPlayback: Boolean = false, startIndex: Int = 0) {
        try {
            queueManager.prepareQueue()
            val mediaItems = queueManager.getMediaItems()

            if (mediaItems.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    player?.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
                    player?.prepare()
                    if (startPlayback) player?.play()
                }
            } else {
                withContext(Dispatchers.Main) {
                    player?.clearMediaItems()
                    player?.stop()
                }
            }
        } catch (e: Exception) {
            Log.e("PlayerService", "Error in loadAndSetQueue", e)
        }
    }

    @SuppressLint("ForegroundServiceType")
    @OptIn(UnstableApi::class)
    private fun setupMediaStyleNotification() {
        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            Commands.NOTIFICATION_ID,
            Commands.CHANNEL_ID
        ).setChannelNameResourceId(R.string.channel_name)
            .setChannelDescriptionResourceId(R.string.channel_description)
            .setMediaDescriptionAdapter(
                object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return player.mediaMetadata.title ?: getString(R.string.notification_default_title)
                }

                @SuppressLint("UnspecifiedImmutableFlag")
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    return createSessionActivityPendingIntent()
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return player.mediaMetadata.artist ?: getString(R.string.notification_default_artist)
                }

                override fun getCurrentLargeIcon(
                    player: Player, callback: PlayerNotificationManager.BitmapCallback
                ): android.graphics.Bitmap? {
                    val mediaMetadata = player.mediaMetadata
                    if (mediaMetadata.artworkUri != null) {
                        val artworkUri = mediaMetadata.artworkUri
                        serviceScope.launch {
                            val bitmap = artworkUri?.let { loadBitmapFromUri(it) }
                            bitmap?.let { callback.onBitmap(it) }
                        }
                    }
                    return BitmapFactory.decodeResource(resources, R.drawable.chirro_1)
                }

                private suspend fun loadBitmapFromUri(uri: Uri): android.graphics.Bitmap? {
                    return try {
                        val result = serviceScope.async(Dispatchers.IO) {
                            val inputStream = contentResolver.openInputStream(uri)
                            inputStream?.use { BitmapFactory.decodeStream(it) }
                        }
                        result.await()
                    } catch (e: Exception) {
                        Log.e("PlayerService", "Error loading bitmap from URI: $uri", e)
                        null
                    }
                }
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    if (ongoing && player != null) {
                        startForeground(notificationId, notification)
                    } else {
                        stopForeground(false)
                    }
                }
                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopSelf()
                }
            }).build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Commands.CHANNEL_ID,
            getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_description)
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createSessionActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        val flags =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(this, 0, intent, flags)
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        playerNotificationManager?.setPlayer(null)
        playerNotificationManager = null
        mediaSession?.release()
        mediaSession = null
        player?.release()
        player = null
        serviceScope.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    @OptIn(UnstableApi::class)
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    @OptIn(UnstableApi::class)
    private inner class PlayerListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {}
        override fun onIsPlayingChanged(isPlaying: Boolean) {}
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {}
        override fun onPlayerError(error: PlaybackException){}
    }
}
