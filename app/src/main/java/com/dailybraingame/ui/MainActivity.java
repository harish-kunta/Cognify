package com.dailybraingame.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dailybraingame.cognify.R;
import com.dailybraingame.util.Constants;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private TextView streakText;
    private TextView nextRefreshTimer;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int currentStreak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        streakText = findViewById(R.id.streakText);
        nextRefreshTimer = findViewById(R.id.nextRefreshTimer);
        MaterialButton wordDashButton = findViewById(R.id.wordDashButton);
        MaterialButton quickMathButton = findViewById(R.id.quickMathButton);
        MaterialButton leaderboardButton = findViewById(R.id.leaderboardButton);

        // Set up click listeners
        wordDashButton.setOnClickListener(v -> startActivity(new Intent(this, WordDashActivity.class)));
        quickMathButton.setOnClickListener(v -> startActivity(new Intent(this, QuickMathActivity.class)));
        leaderboardButton.setOnClickListener(v -> startActivity(new Intent(this, LeaderboardActivity.class)));

        // Initialize streak
        currentStreak = getSharedPreferences("game_prefs", MODE_PRIVATE).getInt("streak", Constants.DEFAULT_STREAK);
        updateStreakDisplay();

        // Set up timer
        timerHandler = new Handler();
        setupTimer();
    }

    private void updateStreakDisplay() {
        streakText.setText(String.format("🔥 Streak: %d days", currentStreak));
    }

    private void setupTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                Calendar nextDay = Calendar.getInstance();
                nextDay.add(Calendar.DAY_OF_MONTH, 1);
                nextDay.set(Calendar.HOUR_OF_DAY, 0);
                nextDay.set(Calendar.MINUTE, 0);
                nextDay.set(Calendar.SECOND, 0);

                long diff = nextDay.getTimeInMillis() - now.getTimeInMillis();
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;

                nextRefreshTimer.setText(String.format("Next refresh in: %02d:%02d:%02d", hours, minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
} 