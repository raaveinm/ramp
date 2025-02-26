package com.raaveinm.ramp;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.support.v4.media.MediaBrowserCompat.MediaItem;

import androidx.media.MediaBrowserServiceCompat;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
 * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
 * exposes it through its MediaSession.Token, which allows the client to create a MediaController
 * that connects to and send control commands to the MediaSession remotely. This is useful for
 * user interfaces that need to interact with your media session, like Android Auto. You can
 * (should) also use the same service from your app's UI, which gives a seamless playback
 * experience to the user.
 * <p>
 * To implement a MediaBrowserService, you need to:
 *
 * <ul>
 *
 * <li> Extend {@link MediaBrowserServiceCompat}, implementing the media browsing
 *      related methods {@link MediaBrowserServiceCompat#onGetRoot} and
 *      {@link MediaBrowserServiceCompat#onLoadChildren};
 * <li> In onCreate, start a new {@link MediaSessionCompat} and notify its parent
 *      with the session"s token {@link MediaBrowserServiceCompat#setSessionToken};
 *
 * <li> Set a callback on the {@link MediaSessionCompat#setCallback(MediaSessionCompat.Callback)}.
 *      The callback will receive all the user"s actions, like play, pause, etc;
 *
 * <li> Handle all the actual music playing using any method your app prefers (for example,
 *      {@link MediaPlayer})
 *
 * <li> Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
 *      {@link MediaSessionCompat#setPlaybackState(PlaybackStateCompat)}
 *      {@link MediaSessionCompat#setMetadata(MediaMetadataCompat)} and
 *      {@link MediaSessionCompat#setQueue(List)})
 *
 * <li> Declare and export the service in AndroidManifest with an intent receiver for the action
 *      android.media.browse.MediaBrowserService
 *
 * </ul>
 * <p>
 * To make your app compatible with Android Auto, you also need to:
 *
 * <ul>
 *
 * <li> Declare a meta-data tag in AndroidManifest.xml linking to a xml resource
 *      with a &lt;automotiveApp&gt; root element. For a media app, this must include
 *      an &lt;uses name="media"/&gt; element as a child.
 *      For example, in AndroidManifest.xml:
 *          &lt;meta-data android:name="com.google.android.gms.car.application"
 *              android:resource="@xml/automotive_app_desc"/&gt;
 *      And in res/values/automotive_app_desc.xml:
 *          &lt;automotiveApp&gt;
 *              &lt;uses name="media"/&gt;
 *          &lt;/automotiveApp&gt;
 *
 * </ul>
 */
public class onPlay extends MediaBrowserServiceCompat {

    private MediaSessionCompat mSession;

    @Override
    public void onCreate() {
        super.onCreate();

        mSession = new MediaSessionCompat(this, "onPlay");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    @Override
    public void onDestroy() {
        mSession.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid,
                                 Bundle rootHints) {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result) {
        result.sendResult(new ArrayList<MediaItem>());
    }

    private static final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
/*
            ImageButton ButtonPlayPause = null;
            ButtonPlayPause = ButtonPlayPause.findViewById(R.id.ButtonPlayPause);
            ButtonPlayPause.setImageResource(R.drawable.ic_media_pause);
*/
        }

        @Override
        public void onPause() {
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
        }

        @Override
        public void onSeekTo(long position) {
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
        }

        @Override
        public void onStop() {
        }

        @Override
        public void onSkipToNext() {
        }

        @Override
        public void onSkipToPrevious() {
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
        }

        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
        }
    }
}