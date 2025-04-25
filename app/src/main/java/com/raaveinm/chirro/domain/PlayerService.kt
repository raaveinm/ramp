package com.raaveinm.chirro.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.core.app.NotificationCompat
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService


class PlayerService: MediaSessionService() {

    private lateinit var player: ExoPlayer
    companion object{
        const val CHANNEL_ID = "PlayerService" }
    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).setAudioAttributes(AudioAttributes.DEFAULT, true).build()
        mediaSession = MediaSession.Builder(this, player).build()
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
//        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(Icons.Filled.Audiotrack)
//            .setContentTitle("Audio is playing")
//            .build()
//
//        startForeground(CHANNEL_ID,notification)
        return START_NOT_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession


    override fun onDestroy() {
        player.release()
        mediaSession.release()
        super.onDestroy()
    }
}