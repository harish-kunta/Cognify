package com.gigamind.cognify.ui;

import static com.gigamind.cognify.util.Constants.BONUS_NEW_PB;
import static com.gigamind.cognify.util.Constants.BONUS_STREAK_PER_DAY;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    private TextView scoreText, streakText, highScoreText, newHighScoreText, xpGainedText, totalXPText;

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

        int xpEarned = calculateXpEarned(score, gameType, isDailyChallenge);

        boolean isNewPb = updateHighScoreLocal(score, gameType);
        int currentStreak = updateStreakLocal(isDailyChallenge);

        displayResults(score, isNewPb, xpEarned, currentStreak);

        if (firebaseUser != null) {
            writeToFirestore(firebaseUser.getUid(), score, xpEarned, gameType, isNewPb, currentStreak);
        }

        // Setup button listeners
        setupButtons(gameType, isDailyChallenge);
    }
    
    private void initializeViews() {
        scoreText = findViewById(R.id.scoreValue);
        streakText = findViewById(R.id.streakText);
        highScoreText = findViewById(R.id.highScoreText);
        newHighScoreText = findViewById(R.id.newHighScoreText);
        xpGainedText    = findViewById(R.id.xpGainedText);
        totalXPText     = findViewById(R.id.totalXPValue);
        playAgainButton = findViewById(R.id.playAgainButton);
        homeButton = findViewById(R.id.homeButton);
    }

    private int calculateXpEarned(int score, String gameType, boolean isDaily) {
        int xp = score;

        // Check if new PB
        String pbKey = "pb_" + gameType.toLowerCase();
        int existingPb = prefs.getInt(pbKey, 0);
        if (score > existingPb) {
            xp += BONUS_NEW_PB;
        }

        // If daily mode and streak bonus
        if (isDaily) {
            // We’ll check last play date:
            String lastDate = prefs.getString("last_played_date", "");
            Calendar cal = Calendar.getInstance();
            String today = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.DAY_OF_YEAR);

            cal.add(Calendar.DAY_OF_YEAR, -1);
            String yesterday = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.DAY_OF_YEAR);

            if (yesterday.equals(lastDate)) {
                // Streak continues → bonus
                xp += BONUS_STREAK_PER_DAY;
            }
        }

        return xp;
    }

    private boolean updateHighScoreLocal(int score, String gameType) {
        String highScoreKey = "high_score_" + gameType.toLowerCase();
        int currentHigh = prefs.getInt(highScoreKey, 0);

        if (score > currentHigh) {
            prefs.edit().putInt(highScoreKey, score).apply();
            newHighScoreText.setVisibility(View.VISIBLE);
            highScoreText.setText(String.valueOf(score));
            return true;
        } else {
            newHighScoreText.setVisibility(View.GONE);
            highScoreText.setText(String.valueOf(currentHigh));
            return false;
        }
    }

    private int updateStreakLocal(boolean isDaily) {
        if (!isDaily) {
            streakText.setVisibility(View.GONE);
            return 0;
        }

        SharedPreferences.Editor editor = prefs.edit();
        Calendar cal = Calendar.getInstance();
        String today = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.DAY_OF_YEAR);
        String lastDate = prefs.getString("last_played_date", "");
        int currentStreak = prefs.getInt("current_streak", 0);

        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.DAY_OF_YEAR);

        if (yesterday.equals(lastDate)) {
            currentStreak++;
        } else if (!today.equals(lastDate)) {
            currentStreak = 1;
        }

        editor.putInt("current_streak", currentStreak);
        editor.putString("last_played_date", today);
        editor.apply();

        streakText.setText("🔥 " + currentStreak + " Day Streak");
        streakText.setVisibility(View.VISIBLE);
        return currentStreak;
    }

    private void displayResults(int score, boolean isNewPb, int xpGained, int currentStreak) {
        scoreText.setText(String.valueOf(score));
        xpGainedText.setText(String.valueOf(xpGained));

        // Show total XP from SharedPreferences
        int totalXP = prefs.getInt("total_xp", 0);
        totalXP += xpGained; // NOTE: we’ll overwrite this in Firestore write as well
        totalXPText.setText(String.valueOf(totalXP));

        // New PB banner is already toggled in updateHighScoreLocal
    }

    private void writeToFirestore(String uid, int score, int xpGained, String gameType,
                                  boolean isNewPb, int currentStreak) {
        DocumentReference userRef = firestore.collection("users").document(uid);

        // 1) Check existing bestWordDashScore in Firestore
        userRef.get().addOnSuccessListener((DocumentSnapshot snapshot) -> {
            int existingBest = 0;
            if (snapshot.exists() && snapshot.contains("bestWordDashScore")) {
                existingBest = snapshot.getLong("bestWordDashScore").intValue();
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("lastWordDashScore", score);

            // If this run is a new PB, update bestWordDashScore
            if (isNewPb && score > existingBest) {
                updates.put("bestWordDashScore", score);
            }

            // Increment totalXP atomically
            updates.put("totalXP", FieldValue.increment(xpGained));

            // Overwrite currentStreak
            updates.put("currentStreak", currentStreak);

            // If they just hit a 7-day streak, award a trophy
            if (currentStreak == 7) {
                updates.put("trophies", FieldValue.arrayUnion("7DayStreak"));
            }

            // Merge these updates into users/{uid}
            userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        // 2) Write a session entry
                        writeSessionEntry(uid, score, xpGained, gameType);
                    })
                    .addOnFailureListener(e -> {
                        // If merging fails, still attempt to write session entry
                        writeSessionEntry(uid, score, xpGained, gameType);
                    });
        }).addOnFailureListener(e -> {
            // If get() fails (rare), still set minimal fields
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastWordDashScore", score);
            updates.put("totalXP", FieldValue.increment(xpGained));
            updates.put("currentStreak", currentStreak);
            userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> writeSessionEntry(uid, score, xpGained, gameType))
                    .addOnFailureListener(ex -> writeSessionEntry(uid, score, xpGained, gameType));
        });
    }

    private void writeSessionEntry(String uid, int score, int xpGained, String gameType) {
        firestore.collection("users")
                .document(uid)
                .collection("sessions")
                .document(gameType.toLowerCase())
                .collection("entries")
                .add(new HashMap<String, Object>() {{
                    put("score", score);
                    put("xpGained", xpGained);
                    put("timestamp", Timestamp.now());
                }})
                .addOnSuccessListener(documentReference -> {
                    // Session entry written successfully
                })
                .addOnFailureListener(e -> {
                    // Failed to write session entry; ignore or log
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