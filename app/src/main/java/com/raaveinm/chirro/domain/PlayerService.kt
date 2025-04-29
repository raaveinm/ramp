package com.raaveinm.chirro.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

import com.raaveinm.chirro.domain.managment.QueueManager


class PlayerService: MediaSessionService() {

    private var player: ExoPlayer? = null
    companion object{
        const val CHANNEL_ID = "PlayerService" }
    private var mediaSession: MediaSession? = null


    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).setAudioAttributes(AudioAttributes.DEFAULT, true).build()
        mediaSession = player?.let { MediaSession.Builder(this, it) }?.build()
        player?.addListener(object: Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    QueueManager(context = this@PlayerService).nextItem()
                }
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun createNotificationChannel(){
        val channel = NotificationChannel(CHANNEL_ID, "Audio Player", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel()
        return START_NOT_STICKY
    }

    fun getPlayer(): ExoPlayer? = player;
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession;


    override fun onDestroy() {
        player?.release()
        mediaSession?.release()
        super.onDestroy()
    }
}