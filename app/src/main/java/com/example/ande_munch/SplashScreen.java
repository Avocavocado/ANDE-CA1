package com.example.ande_munch;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;

import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView textViewPercentage;

    private int progressPercentage = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash); // Add this line to set the content view

        // Initialize xml variables
        progressBar = findViewById(R.id.progress_bar);
        textViewPercentage = findViewById(R.id.text_view_progress);

        // Setting initial view
        progressBar.setProgress(0);
        textViewPercentage.setText("0%");

        updateProgress();
    }

    private void updateProgress() {
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (progressPercentage < 100) {
                    progressPercentage += 1;
                    progressBar.setProgress(progressPercentage);
                    textViewPercentage.setText(progressPercentage + "%");

                    handler.postDelayed(this, 40);
                } else {

                    handler.postDelayed(() -> {
                        startActivity(new Intent(SplashScreen.this, LoginPage.class));
                        finish();
                    }, 500);
                }
            }
        };

        // Start the Runnable immediately
        handler.post(progressRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent memory leaks
        handler.removeCallbacksAndMessages(null);
    }
}
