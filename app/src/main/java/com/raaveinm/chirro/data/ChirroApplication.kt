package com.raaveinm.chirro.data

import android.app.Application

class ChirroApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}