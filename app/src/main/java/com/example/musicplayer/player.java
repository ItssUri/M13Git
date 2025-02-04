package com.example.musicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class player extends AppCompatActivity {
    SeekBar ovmSeekBar;
    MediaPlayer mp = new MediaPlayer();
    private MediaObserver observador = null;
    private boolean isUserSeeking = false; // Flag to pause observer while seeking

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_player);

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        final boolean[] paused = {false};
        ImageButton ovmPlayPauseButton = findViewById(R.id.playPauseImageButton);
        ovmSeekBar = findViewById(R.id.progressBar);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // Get data passed from intent
        ConstraintLayout layout = findViewById(R.id.main);
        Intent intent = getIntent();
        Uri ovmFile = Uri.parse(intent.getStringExtra("fileUri"));

        try {
            mp.setDataSource(String.valueOf(ovmFile));
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            Log.d("INFO", "onCreate: Error my drilla");
            throw new RuntimeException(e);
        }

        // Set album art and background
        byte[] byteArray = getIntent().getByteArrayExtra("albumArt");
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            ImageView imageView = findViewById(R.id.albumArtOvm);
            imageView.setImageBitmap(bitmap);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            bitmapDrawable.setColorFilter(Color.parseColor("#E0000000"), PorterDuff.Mode.SRC_ATOP);
            layout.post(() -> {
                int layoutWidth = layout.getWidth();
                int layoutHeight = layout.getHeight();

                // Calculate the aspect ratios
                float bitmapAspectRatio = (float) bitmap.getWidth() / bitmap.getHeight();
                float layoutAspectRatio = (float) layoutWidth / layoutHeight;

                if (bitmapAspectRatio > layoutAspectRatio) {
                    // Bitmap is wider, scale by height and crop sides
                    int scaledWidth = (int) (layoutHeight * bitmapAspectRatio);
                    bitmapDrawable.setBounds((layoutWidth - scaledWidth) / 2, 0,
                            (layoutWidth + scaledWidth) / 2, layoutHeight);
                } else {
                    // Bitmap is taller, scale by width and crop top and bottom
                    int scaledHeight = (int) (layoutWidth / bitmapAspectRatio);
                    bitmapDrawable.setBounds(0, (layoutHeight - scaledHeight) / 2,
                            layoutWidth, (layoutHeight + scaledHeight) / 2);
                }

                // Set the BitmapDrawable as the background
                layout.setBackground(bitmapDrawable);
            });
            layout.setBackground(bitmapDrawable);

        }

        // Set track info
        String title = intent.getStringExtra("trackName");
        String artist = intent.getStringExtra("artist");
        String albumName = intent.getStringExtra("albumName");

        TextView ovmTrackName = findViewById(R.id.albumInfoTrackName);
        TextView ovmAlbumName = findViewById(R.id.albumInfoTrackAlbum);
        TextView ovmArtistName = findViewById(R.id.albumInfoTrackArtist);

        ovmTrackName.setText(title);
        ovmAlbumName.setText("On " + albumName);
        ovmArtistName.setText(artist);

        // Play/pause button
        ovmPlayPauseButton.setOnClickListener(view -> {
            if (paused[0]) {
                mp.start();
                ovmPlayPauseButton.setImageResource(R.drawable.pause);
            } else {
                mp.pause();
                ovmPlayPauseButton.setImageResource(R.drawable.play);
            }
            paused[0] = !paused[0];
        });

        // Stop button
        ImageButton ovmStopButton = findViewById(R.id.imageButton3);
        ovmStopButton.setOnClickListener(view -> {
            mp.stop();
            try {
                mp.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
            paused[0] = true;
            ovmPlayPauseButton.setImageResource(R.drawable.play);
        });

        // Rewind button
        ImageButton ovmRewindButton = findViewById(R.id.imageButton);
        ovmRewindButton.setOnClickListener(view -> {
            mp.seekTo(0);
            mp.start();
            paused[0] = false;
            ovmPlayPauseButton.setImageResource(R.drawable.pause);
        });

        // Back button
        ImageButton testingBack = findViewById(R.id.backImageButton);
        testingBack.setOnClickListener(view -> {
            mp.stop();
            Intent backIntent = new Intent(player.this, MainActivity.class);
            startActivity(backIntent);
        });

        // SeekBar interaction
        ovmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int newPosition = (int) (mp.getDuration() * (progress / 100.0));
                    mp.seekTo(newPosition);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true; // Pause updates while user is seeking
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false; // Resume updates after user is done
            }
        });

        // MediaObserver updates SeekBar progress
        observador = new MediaObserver();
        new Thread(observador).start();

        // Handle media completion
        mp.setOnCompletionListener(mp -> {
            observador.stop();
            ovmSeekBar.setProgress(0);
            mp.seekTo(0);
            ovmPlayPauseButton.setImageResource(R.drawable.play); // Change button to play icon
            paused[0] = true; // Update paused state
            mp.stop();
            try {
                mp.prepare();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            observador = new MediaObserver();
            new Thread(observador).start();
        });
    }

    private class MediaObserver implements Runnable {
        private final AtomicBoolean stop = new AtomicBoolean(false);
        // Project 2 Changes
        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            while (!stop.get()) {
                if (!isUserSeeking && mp.isPlaying()) {
                    runOnUiThread(() -> {
                        int progress = (int) ((double) mp.getCurrentPosition() / mp.getDuration() * 100);
                        ovmSeekBar.setProgress(progress);
                    });
                }
                try {
                    Thread.sleep(100);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.stop();
        observador.stop();
    }
}
