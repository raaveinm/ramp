package com.raaveinm.chirro.domain.managers

import android.app.PendingIntent
import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.raaveinm.chirro.data.room.DatabaseManager
import com.raaveinm.chirro.domain.usecase.Commands
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class MediaSessionManager(
    private val service: MediaSessionService,
    private val player: Player,
    private val serviceScope: CoroutineScope,
    private val sessionActivity: PendingIntent,
    private val queueLoader: suspend () -> Unit
) {

    var mediaSession: MediaSession? = null
        private set

    private val databaseManager: DatabaseManager by lazy { DatabaseManager() }

    fun initialize() {
        mediaSession = MediaSession.Builder(service, player)
            .setCallback(PlayerSessionCallback())
            .setId("ChirroMediaSession_${System.currentTimeMillis()}")
            .setSessionActivity(sessionActivity)
            .build()
    }

    fun release() {
        mediaSession?.release()
        mediaSession = null
    }

    private inner class PlayerSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
                .add(SessionCommand(Commands.ACTION_REFRESH_QUEUE, Bundle.EMPTY))
                .add(SessionCommand(Commands.ACTION_TOGGLE_FAVORITE, Bundle.EMPTY))
                .build()

            return MediaSession.ConnectionResult.accept(
                availableSessionCommands,
                connectionResult.availablePlayerCommands
            )
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                Commands.ACTION_REFRESH_QUEUE -> {
                    Log.d("MediaSessionManager", "Received REFRESH_QUEUE command")
                    serviceScope.launch { queueLoader() }
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                Commands.ACTION_TOGGLE_FAVORITE -> {
                    val trackId = args.getInt(Commands.EXTRA_TRACK_ID, -1)
                    if (trackId != -1) {
                        serviceScope.launch { toggleFavoriteStatus(trackId) }
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    return Futures.immediateFuture(SessionResult(SessionError.ERROR_INVALID_STATE))
                }
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }

        private suspend fun toggleFavoriteStatus(trackId: Int) {
            withContext(Dispatchers.IO) {
                try {
                    val currentTrack = databaseManager.getTrackById(service, trackId)
                    val newFavoriteStatus = !currentTrack.isFavorite
                    databaseManager.updateTrackFavoriteStatus(service, trackId, newFavoriteStatus)
                } catch (e: Exception) {
                    Log.e("MediaSessionManager", "Error updating favorite status for track $trackId", e)
                }
            }
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.mapNotNull {
                it.requestMetadata.mediaUri?.let { uri ->
                    it.buildUpon().setUri(uri).build()
                }
            }.toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
        }
    }
}