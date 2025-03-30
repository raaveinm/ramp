package com.raaveinm.ramp.services

import android.content.Context
import android.os.Bundle
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.raaveinm.ramp.R

@UnstableApi
class ErrorHandling(private val context: Context, player: Player) : ForwardingPlayer(player) {

    private val listeners: MutableList<Player.Listener> = mutableListOf()
    private var customizedPlaybackException: PlaybackException? = null

    init { player.addListener(ErrorCustomizationListener()) }

    override fun addListener(listener: Player.Listener) { listeners.add(listener) }
    override fun removeListener(listener: Player.Listener) { listeners.remove(listener) }
    override fun getPlayerError(): PlaybackException? { return customizedPlaybackException }

    private inner class ErrorCustomizationListener : Player.Listener {

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            customizedPlaybackException = error?.let { customizePlaybackException(it) }
            listeners.forEach { it.onPlayerErrorChanged(customizedPlaybackException) }
        }

        override fun onPlayerError(error: PlaybackException) {
            listeners.forEach { it.onPlayerError(customizedPlaybackException!!) }
        }

        private fun customizePlaybackException(
            error: PlaybackException,
        ): PlaybackException {
            val buttonLabel: String
            val errorMessage: String
            when (error.errorCode) {
                PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> {
                    buttonLabel = context.getString(R.string.err_button_label_restart_stream)
                    errorMessage = context.getString(R.string.err_msg_behind_live_window)
                }

                else -> {
                    buttonLabel = context.getString(R.string.err_button_label_ok)
                    errorMessage = context.getString(R.string.err_message_default)
                }
            }
            val extras = Bundle()
            extras.putString("button_label", buttonLabel)
            return PlaybackException(errorMessage, error.cause, error.errorCode, extras)
        }

        override fun onEvents(player: Player, events: Player.Events) {
            listeners.forEach {
                it.onEvents(player, events)
            }
        }
    }
}