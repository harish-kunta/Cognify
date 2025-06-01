package com.gigamind.cognify.ui;

import static com.gigamind.cognify.util.Constants.INTENT_SCORE;
import static com.gigamind.cognify.util.Constants.INTENT_TYPE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.gigamind.cognify.R;
import com.google.android.material.button.MaterialButton;
import java.util.Calendar;

public class ResultActivity extends AppCompatActivity {
    
    private TextView scoreText;
    private TextView streakText;
    private TextView highScoreText;
    private TextView newHighScoreText;
    private MaterialButton playAgainButton;
    private MaterialButton homeButton;
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        
        // Initialize views
        initializeViews();
        
        // Get game data from intent
        int score = getIntent().getIntExtra(INTENT_SCORE, 0);
        String gameType = getIntent().getStringExtra(INTENT_TYPE);
        boolean isDailyChallenge = getIntent().getBooleanExtra("IS_DAILY_CHALLENGE", false);
        
        // Initialize SharedPreferences
        prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        
        // Update high score if necessary
        updateHighScore(score, gameType);
        
        // Update streak if it's a daily challenge
        if (isDailyChallenge) {
            updateStreak();
        }
        
        // Display results
        displayResults(score, gameType, isDailyChallenge);
        
        // Setup button listeners
        setupButtons(gameType, isDailyChallenge);
    }
    
    private void initializeViews() {
        scoreText = findViewById(R.id.scoreText);
        streakText = findViewById(R.id.streakText);
        highScoreText = findViewById(R.id.highScoreText);
        newHighScoreText = findViewById(R.id.newHighScoreText);
        playAgainButton = findViewById(R.id.playAgainButton);
        homeButton = findViewById(R.id.homeButton);
    }
    
    private void updateHighScore(int score, String gameType) {
        String highScoreKey = "high_score_" + gameType.toLowerCase();
        int currentHighScore = prefs.getInt(highScoreKey, 0);
        
        if (score > currentHighScore) {
            prefs.edit().putInt(highScoreKey, score).apply();
            newHighScoreText.setVisibility(View.VISIBLE);
        } else {
            newHighScoreText.setVisibility(View.GONE);
        }
        
        highScoreText.setText(String.format("High Score: %d", Math.max(score, currentHighScore)));
    }
    
    private void updateStreak() {
        Calendar calendar = Calendar.getInstance();
        String today = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);
        String lastPlayedDate = prefs.getString("last_played_date", "");
        int currentStreak = prefs.getInt("current_streak", 0);
        
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);
        
        if (lastPlayedDate.equals(yesterday)) {
            // Consecutive day, increment streak
            currentStreak++;
        } else if (!lastPlayedDate.equals(today)) {
            // Streak broken
            currentStreak = 1;
        }
        
        prefs.edit()
            .putInt("current_streak", currentStreak)
            .putString("last_played_date", today)
            .apply();
        
        streakText.setText(String.format("🔥 %d Day Streak", currentStreak));
    }
    
    private void displayResults(int score, String gameType, boolean isDailyChallenge) {
        scoreText.setText(String.format("Score: %d", score));
        
        if (!isDailyChallenge) {
            streakText.setVisibility(View.GONE);
        }
        
        // Update leaderboard
        updateLeaderboard(score, gameType);
    }
    
    private void updateLeaderboard(int score, String gameType) {
        String leaderboardKey = "leaderboard_" + gameType.toLowerCase();
        String scoresStr = prefs.getString(leaderboardKey, "");
        
        // Format: "score1,score2,score3,..."
        String[] scores = scoresStr.isEmpty() ? new String[0] : scoresStr.split(",");
        StringBuilder newScores = new StringBuilder();
        boolean scoreAdded = false;
        
        // Keep top 10 scores
        for (int i = 0; i < Math.min(9, scores.length); i++) {
            int currentScore = Integer.parseInt(scores[i]);
            if (!scoreAdded && score > currentScore) {
                newScores.append(score).append(",");
                scoreAdded = true;
            }
            newScores.append(currentScore).append(",");
        }
        
        if (!scoreAdded && (scores.length < 10)) {
            newScores.append(score);
        } else if (newScores.length() > 0) {
            newScores.setLength(newScores.length() - 1); // Remove trailing comma
        }
        
        prefs.edit().putString(leaderboardKey, newScores.toString()).apply();
    }
    
    private void setupButtons(String gameType, boolean isDailyChallenge) {
        playAgainButton.setOnClickListener(v -> {
            if (!isDailyChallenge) {
                Intent gameIntent = new Intent(this, WordDashActivity.class);
                startActivity(gameIntent);
            }
            finish();
        });
        
        homeButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            finish();
        });
        
        // Disable play again for daily challenges
        playAgainButton.setEnabled(!isDailyChallenge);
        playAgainButton.setText(isDailyChallenge ? "Come Back Tomorrow" : "Play Again");
    }
} 