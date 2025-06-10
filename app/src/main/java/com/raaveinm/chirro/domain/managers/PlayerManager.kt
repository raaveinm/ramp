package com.raaveinm.chirro.domain.managers

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer

class PlayerManager(private val context: Context, private val listener: Player.Listener) {

    var player: ExoPlayer? = null
        private set

    @OptIn(UnstableApi::class)
    fun initialize() {
        player = ExoPlayer.Builder(context)
            .setRenderersFactory(
                DefaultRenderersFactory(context).setExtensionRendererMode(
                    DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                )
            )
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build().also {
                it.addListener(listener)
            }
    }

    fun release() {
        player?.release()
        player = null
    }
}