package com.raaveinm.chirro.domain

import android.os.Handler
import android.os.Looper
import androidx.media3.common.Player

class SleepTimer(private val player: Player) {
    private var _endTime: Long? = null
    private val timeHandler = Handler(Looper.getMainLooper())
    private val stopPlaybackRunnable = Runnable {
        if (player.isPlaying) {
            player.stop()
        }
    }

    fun startCountdown(seconds: Long) {
        timeHandler.removeCallbacks(stopPlaybackRunnable)
        timeHandler.postDelayed(stopPlaybackRunnable, seconds * 1000)
        _endTime = System.currentTimeMillis() + seconds * 1000
    }

    fun stopCountdown() {
        timeHandler.removeCallbacks(stopPlaybackRunnable)
        _endTime = null
    }

    fun getEndTime(): Long? = _endTime
}