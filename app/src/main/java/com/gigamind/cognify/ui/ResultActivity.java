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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {
    
    private TextView scoreText;
    private TextView streakText;
    private TextView highScoreText;
    private TextView newHighScoreText;
    private MaterialButton playAgainButton;
    private MaterialButton homeButton;
    private SharedPreferences prefs;

    // Firestore instance
    private FirebaseFirestore firestore;
    // Current Firebase user
    private FirebaseUser firebaseUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        
        // Initialize views
        initializeViews();
        
        // Initialize SharedPreferences
        prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);

        // Initialize Firestore & Auth
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get game data from intent
        int score = getIntent().getIntExtra(INTENT_SCORE, 0);
        String gameType = getIntent().getStringExtra(INTENT_TYPE);
        boolean isDailyChallenge = getIntent().getBooleanExtra("IS_DAILY_CHALLENGE", false);

        // 1) Update local high‐score and streak
        boolean isNewHigh = updateHighScore(score, gameType);
        updateStreakIfDaily(gameType);
        
        // Display results
        displayResults(score, gameType, isNewHigh);

        // 3) Write to Firestore
        if (firebaseUser != null) {
            writeScoreToFirestore(firebaseUser.getUid(), score, gameType);
        }

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
    
    private boolean updateHighScore(int score, String gameType) {
        String highScoreKey = "high_score_" + gameType.toLowerCase();
        int currentHighScore = prefs.getInt(highScoreKey, 0);
        
        if (score > currentHighScore) {
            prefs.edit().putInt(highScoreKey, score).apply();
            newHighScoreText.setVisibility(View.VISIBLE);
            highScoreText.setText(String.format("High Score: %d", score));
            return true;
        } else {
            newHighScoreText.setVisibility(View.GONE);
            highScoreText.setText(String.format("High Score: %d", currentHighScore));
            return false;
        }
        
    }

    private void updateStreakIfDaily(String gameType) {
        // Suppose your daily challenge is identified by “daily_word_dash”
        if (!"daily_word_dash".equalsIgnoreCase(gameType)) {
            streakText.setVisibility(View.GONE);
            return;
        }

        Calendar calendar = Calendar.getInstance();
        String today = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);
        String lastPlayedDate = prefs.getString("last_played_date", "");
        int currentStreak = prefs.getInt("current_streak", 0);

        calendar.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);

        if (yesterday.equals(lastPlayedDate)) {
            // Consecutive day → increment
            currentStreak++;
        } else if (!today.equals(lastPlayedDate)) {
            // Not consecutive (or first time) → reset to 1
            currentStreak = 1;
        }

        prefs.edit()
                .putInt("current_streak", currentStreak)
                .putString("last_played_date", today)
                .apply();

        streakText.setText(String.format("🔥 %d Day Streak", currentStreak));
        streakText.setVisibility(View.VISIBLE);
    }

    private void displayResults(int score, String gameType, boolean isNewHigh) {
        scoreText.setText(String.format("Score: %d", score));
    }

    private void writeScoreToFirestore(String uid, int score, String gameType) {
        // 1) Reference to the user document
        DocumentReference userRef = firestore
                .collection("users")
                .document(uid);

        // 2) Build the map of fields to merge into user document
        Map<String, Object> updates = new HashMap<>();
        if ("word_dash".equalsIgnoreCase(gameType) || "daily_word_dash".equalsIgnoreCase(gameType)) {
            updates.put("lastWordDashScore", score);
        }
        // You can similarly handle other game types (e.g., quick_math) by checking gameType

        // 3) Also update bestWordDashScore if needed
        userRef.get().addOnSuccessListener(snapshot -> {
            int existingBest = 0;
            if (snapshot.exists() && snapshot.contains("bestWordDashScore")) {
                existingBest = snapshot.getLong("bestWordDashScore").intValue();
            }
            if (score > existingBest) {
                updates.put("bestWordDashScore", score);
            }
            // 4) Merge into users/{uid}
            userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        // 5) Now push a leader‐board entry
                        pushToLeaderboard(uid, score, gameType);
                    })
                    .addOnFailureListener(e -> {
                        // Log or Toast a failure, but we still attempt to push to leaderboard:
                        pushToLeaderboard(uid, score, gameType);
                    });
        }).addOnFailureListener(e -> {
            // In case get() fails, we still attempt to write whatever we know:
            userRef.set(updates, com.google.firebase.firestore.SetOptions.merge());
            pushToLeaderboard(uid, score, gameType);
        });
    }

    private void pushToLeaderboard(String uid, int score, String gameType) {
        CollectionReference lbRef = firestore
                .collection("users")
                .document(uid)
                .collection("leaderboard")
                .document(gameType.toLowerCase())
                .collection("entries");

        Map<String, Object> entry = new HashMap<>();
        entry.put("score", score);
        entry.put("timestamp", com.google.firebase.Timestamp.now());

        lbRef.add(entry)
                .addOnSuccessListener(docRef -> {
                    // Successfully added to leaderboard. No need to do anything else here.
                })
                .addOnFailureListener(e -> {
                    // Log or swallow—leaderboard push failure shouldn’t block the user
                });
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