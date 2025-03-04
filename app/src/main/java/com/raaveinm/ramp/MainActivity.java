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
 * 1. How to export value of playPauseStatus to another lifeCycle?
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
 * 6. not VISIBLE/INVISBLE? 8???????
 * 7. How to create app logo?
 * 8. How to ,ake smooth transition in two photos or smooth disappearing of button (for example)?
 * 9. RecyclerView     com.raaveinm.ramp       E  No adapter attached; skipping layout
 * 10. Track Info throws error on string
 */

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    MediaPlayer player = new MediaPlayer();
    int[] backgroundResourceIds = {
            R.drawable.defaulti,
            R.drawable.defaultii,
            R.drawable.defaultiii,
            R.drawable.defaultiv
    };

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

        ImageButton buttonPlayPause = findViewById(R.id.ButtonPlayPause);
        TextView nowPlaying = findViewById(R.id.textView);
        SeekBar seekBar = findViewById(R.id.seekBar);
        RecyclerView menu = findViewById(R.id.Menu);
        ImageView songCover = findViewById(R.id.SongCover);
        ImageButton buttonNext = findViewById(R.id.ButtonNext);
        ImageButton buttonPrevious = findViewById(R.id.ButtonPrevious);
        ImageButton sortButton = findViewById(R.id.SortButton);
        ImageButton buttonMenu = findViewById(R.id.ButtonMenu);

        songCover.setImageResource(getRandom(backgroundResourceIds));

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.faint);

        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        /*
        String url = "https://drive.google.com/file/d/10Z3Bi8yoCAZCTywATNfHfxKkpjc8GBWK/view?usp=sharing";
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );*/

        try {
            player.setDataSource(getApplicationContext(), uri);
            //player.setDataSource(url);
            player.prepareAsync();
        } catch (IOException e) {
            Log.e("PLAYER-ERR", "there " + e.getMessage());
        }

        Log.i(TAG, "onStart called");

        buttonPlayPause.setOnClickListener(v->{
            if (!player.isPlaying()){

                player.start();

                buttonPlayPause.setImageResource(R.drawable.pause_20px);

                Log.v("Player","media is playing. or not. whatever.");
                Log.i("Player Session ID", String.valueOf(this.player.getAudioSessionId()));
            }else{
                player.pause();
                buttonPlayPause.setImageResource(R.drawable.play_arrow_20px);

                Log.v("Player","media is stopped. or not. whatever.");
            }
        });

        sortButton.setOnClickListener(v->{
            player.stop();
            buttonPlayPause.setImageResource(R.drawable.play_arrow_20px);
            songCover.setImageResource(getRandom(backgroundResourceIds));
        });

        player.setOnPreparedListener(mp->{
            try {
                nowPlaying.setText(ExtMethods.getTrackInfo(uri));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            seekBar.setMax(player.getDuration());
        });

        Log.i("menu status", String.valueOf(menu.getVisibility()));

        buttonMenu.setOnClickListener(v->{
            if (menu.getVisibility() == View.GONE){
                menu.setVisibility(View.VISIBLE);
                Log.i("menu status", "menu is visible");
            }else{
                menu.setVisibility(View.GONE);
                Log.i("menu status", "menu is hidden");
            }
        });

        buttonNext.setOnClickListener(v->{
            songCover.setImageResource(getRandom(backgroundResourceIds));
        });

        buttonPrevious.setOnClickListener(v->{
            songCover.setImageResource(getRandom(backgroundResourceIds));
        });

        super.onStart();
    }

    protected void onPause(){
        super.onPause();
    }

    protected void onResume(){
        Log.i(TAG, "onResume called");

        RecyclerView menu = findViewById(R.id.Menu);
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