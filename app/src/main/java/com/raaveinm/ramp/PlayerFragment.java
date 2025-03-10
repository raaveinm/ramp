package com.raaveinm.ramp;

import static com.raaveinm.ramp.ext.ExtMethods.getRandom;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;

public class PlayerFragment extends Fragment {
    private MediaPlayer player;
    private ImageView songCover;
    private ImageButton buttonPlayPause;
    private ImageButton buttonNext;
    private ImageButton buttonPrevious;
    private ImageButton shuffle;
    private TextView nowPlaying;
    private SeekBar seekBar;
    private Boolean isShuffle = false;

    private final int[] backgroundResourceIds = {
            R.drawable.defaulti,
            R.drawable.defaultii,
            R.drawable.defaultiii,
            R.drawable.defaultiv
    };
    String TAG = "PlayerFragment";

    public PlayerFragment() {}

    public static PlayerFragment newInstance(String param1, String param2) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        songCover = view.findViewById(R.id.SongCover);
        buttonPlayPause = view.findViewById(R.id.ButtonPlayPause);
        buttonNext = view.findViewById(R.id.ButtonNext);
        buttonPrevious = view.findViewById(R.id.ButtonPrevious);
        shuffle = view.findViewById(R.id.shuffle);
        nowPlaying = view.findViewById(R.id.textView);
        seekBar = view.findViewById(R.id.seekBar);

        songCover.setImageResource(getRandom(backgroundResourceIds));

        return view;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        player = new MediaPlayer();
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );


        Uri uri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.faint);
        try {
            player.setDataSource(requireContext(), uri);
            player.prepareAsync();
        } catch (IOException e) {
            Log.e("PLAYER-ERR", "Error setting data source: " + e.getMessage());
        }

        buttonPlayPause.setOnClickListener(v -> {
            if (player != null) {
                if (!player.isPlaying()) {
                    player.start();
                    buttonPlayPause.setImageResource(R.drawable.pause);
                    Log.v("Player", "media is playing.");
                    Log.i("Player Session ID", String.valueOf(player.getAudioSessionId()));
                } else {
                    player.pause();
                    buttonPlayPause.setImageResource(R.drawable.play_arrow);
                    Log.v("Player", "media is stopped.");
                }
            }
        });

        shuffle.setOnClickListener(v -> {
            Shuffle(isShuffle);
            isShuffle = !isShuffle;
        });

        buttonNext.setOnClickListener(v -> {
            songCover.setImageResource(getRandom(backgroundResourceIds));
        });

        buttonPrevious.setOnClickListener(v -> {
            songCover.setImageResource(getRandom(backgroundResourceIds));
        });
        player.setOnPreparedListener(mp -> {
            nowPlaying.setText("TrackName will be there");
            seekBar.setMax(player.getDuration());
        });

        new Thread(() -> {
            while (player != null) {
                try {
                    if (player.isPlaying()) {
                        int currentPosition = player.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG, "onStart called");
    }

    public void Shuffle (Boolean isShuffle) {
        if (!isShuffle) {
            ImageButton shuffle = getActivity().findViewById(R.id.shuffle);
            shuffle.setImageResource(R.drawable.shuffle_on);
        }else {
            ImageButton shuffle = getActivity().findViewById(R.id.shuffle);
            shuffle.setImageResource(R.drawable.shuffle);
        }
    }
}