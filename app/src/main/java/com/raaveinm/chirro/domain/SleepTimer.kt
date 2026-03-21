package com.raaveinm.chirro.domain

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.media3.common.Player
import com.raaveinm.chirro.R

class SleepTimer(private val player: Player, private val context: Context) {
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
        val message = context.getString(R.string.timer_set, seconds)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun stopCountdown() {
        timeHandler.removeCallbacks(stopPlaybackRunnable)
        _endTime = null
        Toast.makeText(context, R.string.timer_cancelled, Toast.LENGTH_SHORT).show()
    }

    fun getEndTime(): Long? = _endTime
}