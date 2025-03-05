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
 * 5. Why did it throws error, if declaration was made in commented section?
 * FATAL EXCEPTION: main (Ask Gemini)
 * Process: com.raaveinm.ramp, PID: 12826
 * java.lang.RuntimeException: Unable to instantiate activity ComponentInfo
 * {com.raaveinm.ramp/com.raaveinm.ramp.MainActivity}: java.lang.NullPointerException:
 * Attempt to invoke virtual method 'android.content.pm.ApplicationInfo
 * android.content.Context.getApplicationInfo()' on a null object reference
 * MENU status             com.raaveinm.ramp                    I  8
 * 6. How to create app logo?
 * 7. How to make smooth transition in two photos or smooth disappearing of button (for example)?
 * 8. RecyclerView     com.raaveinm.ramp       E  No adapter attached; skipping layout
 * 9. How to get Track Info?
 */

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    MediaPlayer player = new MediaPlayer();


    /*
        question 5. I meant here that declaration was made in commented section.

        ImageButton buttonPlayPause = findViewById(R.id.ButtonPlayPause);
        TextView nowPlaying = findViewById(R.id.textView);
        SeekBar seekBar = findViewById(R.id.seekBar);
        RecyclerView menu = findViewById(R.id.Menu);


        ImageButton ButtonNext = findViewById(R.id.ButtonNext);
        ImageButton ButtonPrevious = findViewById(R.id.ButtonPrevious);
        ImageButton SortButton = findViewById(R.id.SortButton);
        ImageButton ButtonMenu = findViewById(R.id.ButtonMenu);
    */
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