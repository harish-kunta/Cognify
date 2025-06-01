package com.gigamind.cognify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.gigamind.cognify.R;
import com.google.android.material.button.MaterialButton;
import java.util.Calendar;
import android.media.MediaPlayer;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
    
    private CardView dailyChallengeCard;
    private TextView dailyChallengeTitle;
    private MaterialButton playDailyChallengeButton;
    private MaterialButton playWordDashButton;
    private MaterialButton playQuickMathButton;
    private MediaPlayer buttonSound;
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        initializeViews();
        
        // Initialize sound effects
        //buttonSound = MediaPlayer.create(this, R.raw.button_click);

        // Initialize SharedPreferences
        prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        
        // Set up daily challenge
        setupDailyChallenge();
        
        // Set up click listeners with animations
        setupClickListeners();
    }
    
    private void initializeViews() {
        dailyChallengeCard = findViewById(R.id.dailyChallengeCard);
        dailyChallengeTitle = findViewById(R.id.dailyChallengeTitle);
        playDailyChallengeButton = findViewById(R.id.playDailyChallengeButton);
        playWordDashButton = findViewById(R.id.playWordDashButton);
        playQuickMathButton = findViewById(R.id.playQuickMathButton);
    }
    
    private void setupDailyChallenge() {
        // Determine today's challenge type based on day of week
        Calendar calendar = Calendar.getInstance();
        boolean isWordDay = calendar.get(Calendar.DAY_OF_WEEK) % 2 == 0;
        
        String challengeType = isWordDay ? "Word Dash" : "Quick Math";
        dailyChallengeTitle.setText(challengeType);
        
        // Check if daily challenge is already completed
        String today = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);
        boolean isDailyCompleted = prefs.getBoolean("daily_completed_" + today, false);
        
        if (isDailyCompleted) {
            playDailyChallengeButton.setText("Completed Today");
            playDailyChallengeButton.setEnabled(false);
        }
        
        // Disable the corresponding game in More Games section if it's the daily challenge
        if (isWordDay) {
            playWordDashButton.setEnabled(!isDailyCompleted);
            playWordDashButton.setText(isDailyCompleted ? "Already Played Today" : "Play");
        } else {
            playQuickMathButton.setEnabled(!isDailyCompleted);
            playQuickMathButton.setText(isDailyCompleted ? "Already Played Today" : "Play");
        }
    }
    
    private void setupClickListeners() {
        View.OnClickListener animatedClickListener = v -> {
            
            // Apply bounce animation
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_bounce));
            
            // Handle click after animation delay
            v.postDelayed(() -> handleGameLaunch(v), 200);
        };
        
        playDailyChallengeButton.setOnClickListener(animatedClickListener);
        playWordDashButton.setOnClickListener(animatedClickListener);
        playQuickMathButton.setOnClickListener(animatedClickListener);
    }
    
    private void handleGameLaunch(View v) {
        Intent intent = new Intent(this, WordDashActivity.class);
        boolean isDaily = v.getId() == R.id.playDailyChallengeButton;
        
        if (v.getId() == R.id.playWordDashButton || 
            (isDaily && dailyChallengeTitle.getText().toString().equals("Word Dash"))) {
            intent.putExtra("GAME_TYPE", "WORD");
        } else {
            intent.putExtra("GAME_TYPE", "MATH");
        }
        
        intent.putExtra("IS_DAILY_CHALLENGE", isDaily);
        startActivity(intent);
        
        // If it's a daily challenge, mark it as completed
        if (isDaily) {
            Calendar calendar = Calendar.getInstance();
            String today = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);
            prefs.edit().putBoolean("daily_completed_" + today, true).apply();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh daily challenge status
        setupDailyChallenge();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (buttonSound != null) {
            buttonSound.release();
            buttonSound = null;
        }
    }
}