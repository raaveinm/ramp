package com.raaveinm.ramp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * List of questions
 * 1. How to export value of playPauseStatus to another lifeCycle?
 * 2. How to Import media from local storage?
 * 3. How to Create media query?
 * 4. How to optimize usage of resources?
 * 5. Why did it throws error, if declaration was made in commented section?
 * FATAL EXCEPTION: main (Ask Gemini)
 * Process: com.raaveinm.ramp, PID: 12826
 * java.lang.RuntimeException: Unable to instantiate activity ComponentInfo{com.raaveinm.ramp/com.raaveinm.ramp.MainActivity}: java.lang.NullPointerException: Attempt to invoke virtual method 'android.content.pm.ApplicationInfo android.content.Context.getApplicationInfo()' on a null object reference
 *
 * MENU status             com.raaveinm.ramp                    I  8
 * 6. not VISIBLE/INVISBLE? 8???????
 */

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";

    Boolean playPauseStatus = false;
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
    }



    @SuppressLint("PrivateResource")
    protected void onStart(){
        super.onStart();

        ImageButton buttonPlayPause = findViewById(R.id.ButtonPlayPause);
        TextView nowPlaying = findViewById(R.id.textView);
        SeekBar seekBar = findViewById(R.id.seekBar);
        RecyclerView menu = findViewById(R.id.Menu);
/*
        try {
            player.setDataSource("assets/RIOT-Overkill.flac");
            player.prepareAsync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
*/
        Log.i(TAG, "onStart called");


        buttonPlayPause.setOnClickListener(v->{
            if (!playPauseStatus){
                playPauseStatus = true;
                player.start();

                buttonPlayPause.setImageResource(android.R.drawable.ic_media_pause);

                Log.v("Player","media is playing. or not. whatever.");
                Log.i("Player Session ID", String.valueOf(player.getAudioSessionId()));
            }else{
                playPauseStatus = false;
                player.pause();

                buttonPlayPause.setImageResource(android.R.drawable.ic_media_play);

                Log.v("Player","media is stopped. or not. whatever.");
            }
        });

        player.setOnPreparedListener(mp->{
            nowPlaying.setText(player.getCurrentPosition());
            seekBar.setMax(player.getDuration());
        });

        Log.i("menu status", String.valueOf((menu.getVisibility())));

        menu.setOnClickListener(v->{
            if (menu.getVisibility() == View.INVISIBLE){
                menu.setVisibility(View.VISIBLE);
                Log.i("menu status", "menu is visible");
            }else{
                menu.setVisibility(View.INVISIBLE);
                Log.i("menu status", "menu is hidden");
            }
        });
    }

    protected void onPause(){
        super.onPause();
    }

    protected void onResume(){
        super.onResume();
        Log.i(TAG, "onResume called");

        RecyclerView menu = findViewById(R.id.Menu);

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