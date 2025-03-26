package com.raaveinm.ramp.snippets

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private const val TAG = "VideoPlayerController"

class VideoPlayerController(
    private val context: Context,
    private val playerView: PlayerView,
    lifecycleOwner: LifecycleOwner
) : DefaultLifecycleObserver {

    var player: ExoPlayer? = null
        private set
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L

    private val playbackStateListener: Player.Listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d(TAG, "changed state to $stateString")
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Player Error: ${error.message}", error)
        }
    }

    init { lifecycleOwner.lifecycle.addObserver(this) }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "onStart: Initializing Player")
        initializePlayer()
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "onStop: Releasing Player")
        releasePlayer()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "onDestroy: Removing observer")
        owner.lifecycle.removeObserver(this)
        releasePlayer()
    }

    /**
     * Loads a list of MediaItems into the player.
     * Call this after the controller is initialized (e.g., in Activity's onCreate/onStart).
     *
     * @param mediaItems The list of MediaItems to play.
     * @param startItemIndex The index of the item to start playback from.
     * @param startPositionMs The position within the start item to begin playback (in milliseconds).
     */
    fun loadMedia(
        mediaItems: List<MediaItem>,
        startItemIndex: Int = 0,
        startPositionMs: Long = 0L
    ) {
        player?.let { exoPlayer ->
            // Update the start index and position before setting new items
            mediaItemIndex = startItemIndex
            playbackPosition = startPositionMs
            exoPlayer.setMediaItems(mediaItems, mediaItemIndex, playbackPosition)
            exoPlayer.prepare() // Prepare the player with the new media
            Log.d(TAG, "Media loaded. Items: ${mediaItems.size}, StartIndex: $startItemIndex, StartPos: $startPositionMs")
        } ?: Log.w(TAG, "loadMedia called but player is not initialized yet.")
    }

    /**
     * Loads a single MediaItem into the player.
     *
     * @param mediaItem The MediaItem to play.
     * @param startPositionMs The position to begin playback (in milliseconds).
     */
    fun loadMedia(mediaItem: MediaItem, startPositionMs: Long = 0L) {
        loadMedia(listOf(mediaItem), 0, startPositionMs)
    }

    fun play() { player?.play() }

    fun pause() { player?.pause() }

    fun seekTo(positionMs: Long) { player?.seekTo(positionMs) }

    fun seekToDefaultPosition() { player?.seekToDefaultPosition() }

    private fun initializePlayer() {
        // If player already exists, no need to re-initialize
        if (player != null) {
            Log.d(TAG, "Player already initialized.")
            return
        }

        player = ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                playerView.player = exoPlayer // Bind player to the view
                // Configure player options (Example: Max video size SD)
                exoPlayer.trackSelectionParameters =
                    exoPlayer.trackSelectionParameters.buildUpon().setMaxVideoSizeSd().build()
                // Restore state before setting media (important if media is loaded later)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.addListener(playbackStateListener)
                // prepare() is now called in loadMedia() after setting items
                Log.i(TAG, "ExoPlayer initialized.")
            }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            // Save state before releasing
            playbackPosition = exoPlayer.currentPosition
            mediaItemIndex = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady

            // Release resources
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
            Log.i(TAG, "ExoPlayer released. Saved state: Index=$mediaItemIndex, Pos=$playbackPosition, PlayWhenReady=$playWhenReady")
        }
        player = null
        playerView.player = null // Important to detach from view
    }
}