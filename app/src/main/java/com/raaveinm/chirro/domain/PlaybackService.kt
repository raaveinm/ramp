package com.raaveinm.chirro.domain

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.raaveinm.chirro.data.ChirroApplication
import com.raaveinm.chirro.MainActivity
import com.raaveinm.chirro.data.datastore.SettingDataStoreRepository
import com.raaveinm.chirro.data.repository.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Callable

@UnstableApi
class PlaybackService : MediaLibraryService() {

    private lateinit var player: ExoPlayer
    private lateinit var session: MediaLibrarySession
    private lateinit var trackRepository: TrackRepository
    private lateinit var notificationProvider: PlaybackNotificationProvider
    private lateinit var sleepTimer: SleepTimer

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var settingsRepository: SettingDataStoreRepository

    override fun onCreate() {
        super.onCreate()
        val container = (application as ChirroApplication).container
        trackRepository = container.trackRepository
        settingsRepository = container.settingsRepository

        notificationProvider = PlaybackNotificationProvider(this)
        setMediaNotificationProvider(notificationProvider)

        // Build Player
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                saveCurrentState()
            }
        })

        sleepTimer = SleepTimer(player)

        // Build Session
        session = MediaLibrarySession
            .Builder(this, player, LibrarySessionCallback())
            .setSessionActivity(getSingleTopActivity())
            .build()
    }

    private fun getSingleTopActivity(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo)
            : MediaLibrarySession { return session }

    override fun onDestroy() {
        session.release()
        player.release()
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun saveCurrentState() {
        serviceScope.launch {
            try {
                val prefs = settingsRepository.settingsPreferencesFlow.first()
                if (!prefs.isSavedState) return@launch
                val currentMediaId = player.currentMediaItem?.mediaId?.toIntOrNull() ?: return@launch
                val track = trackRepository.getTrackById(currentMediaId)
                settingsRepository.updateCurrentTrack(track)
            } catch (e: Exception) {
                android.util.Log.e("PlaybackService", "Failed to save state: ${e.message}")
            }
        }
    }

    ///////////////////////////////////////////////
    // Alarm Manager (Sleep timer)
    ///////////////////////////////////////////////
    private inner class LibrarySessionCallback : MediaLibrarySession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommand = SessionCommand(START_SLEEP_TIMER, Bundle.EMPTY)
            val sessionCommand2 = SessionCommand(STOP_SLEEP_TIMER, Bundle.EMPTY)
            val availableSessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(sessionCommand)
                .add(sessionCommand2)
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(availableSessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                START_SLEEP_TIMER -> {
                    val seconds = args.getLong(EXTRA_SECONDS, 0L)
                    if (seconds > 0) {
                        sleepTimer.startCountdown(seconds)
                        val resultExtras = Bundle().apply {
                            sleepTimer.getEndTime()?.let { putLong("END_TIME", it) }
                        }
                        return Futures.immediateFuture(
                            SessionResult(SessionResult.RESULT_SUCCESS, resultExtras)
                        )
                    }
                }
                STOP_SLEEP_TIMER -> {
                    sleepTimer.stopCountdown()
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }

        ///////////////////////////////////////////////
        ///////////////////////////////////////////////

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId(ROOT_ID)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("Chirro Music")
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            if (parentId != ROOT_ID) {
                return Futures.immediateFuture(
                    LibraryResult.ofItemList(
                        ImmutableList.of(),
                        params
                    )
                )
            }

            // Using Guava Futures to bridge Coroutines -> ListenableFuture
            return Futures.submit(
                Callable {
                    importMediaItems(page, pageSize)
                }
            ) { command -> serviceScope.launch(Dispatchers.IO) { command.run() } }
        }

        private fun importMediaItems(
            page: Int,
            pageSize: Int
        ): LibraryResult<ImmutableList<MediaItem>> {
            return try {
                val tracks = runBlocking {
                    trackRepository.getTracksPaged(page, pageSize)
                }

                val mediaItems = tracks.map { it.toMediaItem() }
                LibraryResult.ofItemList(
                    ImmutableList.copyOf(mediaItems),
                    null
                )
            } catch (_: Exception) {
                LibraryResult.ofError(SessionError.ERROR_UNKNOWN)
            }
        }
    }

    companion object {
        private const val ROOT_ID = "root"
        const val START_SLEEP_TIMER = "START_SLEEP_TIMER"
        const val STOP_SLEEP_TIMER = "STOP_SLEEP_TIMER"
        const val EXTRA_SECONDS = "EXTRA_SECONDS"
    }
}
