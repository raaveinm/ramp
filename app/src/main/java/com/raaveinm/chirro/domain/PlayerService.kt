package com.raaveinm.chirro.domain

import android.app.Service
import android.content.Intent
import android.os.IBinder


class PlayerService: Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }


}