package com.raaveinm.chirro.domain

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.raaveinm.chirro.MainActivity
import com.raaveinm.chirro.data.room.DatabaseManager
import com.raaveinm.chirro.domain.managers.MediaSessionManager
import com.raaveinm.chirro.domain.managers.PlayerManager
import com.raaveinm.chirro.domain.managment.QueueManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class PlayerService : MediaSessionService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val databaseManager: DatabaseManager by lazy { DatabaseManager() }
    private val queueManager: QueueManager by lazy { QueueManager(this, databaseManager) }

    private lateinit var playerManager: PlayerManager
    private lateinit var mediaSessionManager: MediaSessionManager

    override fun onCreate() {
        super.onCreate()

        playerManager = PlayerManager(this, PlayerListener())
        playerManager.initialize()
        mediaSessionManager = MediaSessionManager(
            this,
            playerManager.player!!, serviceScope,
            createSessionActivityPendingIntent()) { loadAndSetQueue() }
        mediaSessionManager.initialize()
        serviceScope.launch { loadAndSetQueue() }
    }

    private suspend fun loadAndSetQueue(startPlayback: Boolean = false, startIndex: Int = 0) {
        try {
            queueManager.prepareQueue()
            val mediaItems = queueManager.getMediaItems()

            withContext(Dispatchers.Main) {
                if (mediaItems.isNotEmpty()) {
                    playerManager.player?.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
                    playerManager.player?.prepare()
                    if (startPlayback) playerManager.player?.play()
                } else {
                    playerManager.player?.clearMediaItems()
                    playerManager.player?.stop()
                }
            }
        } catch (e: Exception) {
            Log.e("PlayerService", "Error in loadAndSetQueue", e)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createSessionActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(this, 0, intent, flags)
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
        mediaSessionManager.release()
        serviceScope.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSessionManager.mediaSession

    private inner class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            //  service lifecycle based on player state
        }
    }
}