package com.raaveinm.chirro.domain

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList

@UnstableApi
class PlaybackNotificationProvider(context: Context) : DefaultMediaNotificationProvider(context) {
    override fun getMediaButtons(
        session: MediaSession,
        playerCommands: androidx.media3.common.Player.Commands,
        customLayout: ImmutableList<CommandButton>,
        showPauseButton: Boolean
    ): ImmutableList<CommandButton> {
        return super.getMediaButtons(
            session,
            playerCommands,
            customLayout,
            showPauseButton
        )
    }
}
