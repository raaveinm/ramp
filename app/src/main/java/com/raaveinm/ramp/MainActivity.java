package com.raaveinm.ramp;

import static com.raaveinm.ramp.ext.ExtMethods.getRandom;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import android.media.MediaMetadataRetriever;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;

import com.raaveinm.ramp.ext.ExtMethods;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * List of questions
 * 1. How to export value of playPauseStatus to another lifeCycle? (solved)
 * 2. How to Import media from local storage?
 * 3. How to Create media query?
 * 4. How to optimize usage of resources?
 * 5. Why did it throws error, if declaration was made in commented section? (solved)
 * 6. How to create app logo?
 * 7. How to make smooth transition in two photos or smooth disappearing of button (for example)?
 * 8. RecyclerView     com.raaveinm.ramp       E  No adapter attached; skipping layout
 * 9. How to get Track Info?
 */

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    MediaPlayer player = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        PlayerFragment.newInstance("null","null");
    }


    @SuppressLint("PrivateResource")
    protected void onStart(){
        super.onStart();
    }

    protected void onPause(){
        super.onPause();
    }

    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}