package com.raaveinm.ramp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.raaveinm.ramp.onPlay;

public class MainActivity extends AppCompatActivity {

    ImageButton ButtonPlayPause = findViewById(R.id.ButtonPlayPause);
    ImageButton ButtonNext = findViewById(R.id.ButtonNext);
    ImageButton ButtonPrevious = findViewById(R.id.ButtonPrevious);
    ImageButton SortButton = findViewById(R.id.SortButton);
    ImageButton ButtonMenu = findViewById(R.id.ButtonMenu);

    Integer PlayPauseStatus = 0;

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

        ButtonPlayPause.setOnClickListener(v -> {
            if (PlayPauseStatus == 0){
                onPlay.MediaSessionCallback.onPlay();
                PlayPauseStatus = 1;

            }
            else{
                onPlay.MediaSessionCallback.onPause();
                PlayPauseStatus = 0;
            }
        });
    }
}