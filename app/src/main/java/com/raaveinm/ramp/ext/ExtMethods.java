package com.raaveinm.ramp.ext;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.Random;



public class ExtMethods {
    public static int getRandom(int[] ToSort) {
        int rnd = new Random().nextInt(ToSort.length);
        return ToSort[rnd];
    }

    public static String getTrackInfo(Uri uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //retriever.setDataSource(getApplicationContext(), uri);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            return title + " - " + artist;
        } catch (Exception e) {
            Log.e("TrackInfo", "Error getting track info: " + e.getMessage());
            return "Unknown Track - Unknown Artist";
        } finally {
            retriever.release();
        }
    }
}
