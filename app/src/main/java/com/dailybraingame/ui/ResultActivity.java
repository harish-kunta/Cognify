package com.dailybraingame.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dailybraingame.cognify.R;
import com.dailybraingame.util.Constants;
import com.google.android.material.button.MaterialButton;

public class ResultActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get game results from intent
        Intent intent = getIntent();
        int score = intent.getIntExtra(Constants.INTENT_SCORE, 0);
        int time = intent.getIntExtra(Constants.INTENT_TIME, 0);

        // Initialize views
        TextView finalScoreText = findViewById(R.id.finalScoreText);
        TextView timeText = findViewById(R.id.timeText);
        TextView streakText = findViewById(R.id.streakText);
        MaterialButton shareButton = findViewById(R.id.shareButton);
        MaterialButton doneButton = findViewById(R.id.doneButton);

        // Update UI
        finalScoreText.setText("Score: " + score);
        timeText.setText("Time: " + time + "s");

        // Handle streak
        prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        int currentStreak = prefs.getInt("streak", 0);
        long lastPlayTime = prefs.getLong("last_play", 0);
        
        // Update streak if it's a new day
        long now = System.currentTimeMillis();
        if (now - lastPlayTime > 24 * 60 * 60 * 1000) {
            currentStreak++;
            prefs.edit()
                .putInt("streak", currentStreak)
                .putLong("last_play", now)
                .apply();
        }

        streakText.setText(String.format("🔥 Streak: %d days", currentStreak));

        // Set up share button
        int finalCurrentStreak = currentStreak;
        shareButton.setOnClickListener(v -> shareScore(score, finalCurrentStreak));

        // Set up done button
        doneButton.setOnClickListener(v -> {
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(mainIntent);
            finish();
        });
    }

    private void shareScore(int score, int streak) {
        String shareText = String.format(
            "I just scored %d points in Cognify! 🧠\n" +
            "Current streak: %d days 🔥\n" +
            "Can you beat my score?",
            score, streak
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share your score"));
    }
} 