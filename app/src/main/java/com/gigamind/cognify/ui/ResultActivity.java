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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
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

        int xpEarned = calculateXpEarned(score, gameType);

        boolean isNewPb = updateHighScoreLocal(score, gameType);
        if (firebaseUser != null) {
            syncStreakWithFirestore(
                    firebaseUser.getUid(),
                    score,
                    xpEarned,
                    gameType,
                    isNewPb
            );
        } else {
            // Not signed in: fallback to strictly SharedPref update
            int newStreak = updateStreakLocallyOnly();
            displayResults(score, isNewPb, xpEarned, newStreak);
        }

        // Setup button listeners
        setupButtons(gameType);
    }

    private int calculateXpEarned(int score, String gameType) {
        int xp = score;

        // PB check
        String pbKey = "pb_" + gameType.toLowerCase();
        int existingPb = prefs.getInt(pbKey, 0);
        if (score > existingPb) {
            xp += BONUS_NEW_PB;
        }

        // Optional: if they played yesterday (according to SharedPrefs), grant streak XP bonus
        String lastDate = prefs.getString("last_played_date", "");
        if (!lastDate.isEmpty()) {
            Calendar cal = Calendar.getInstance();
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(cal.getTime());

            cal.add(Calendar.DAY_OF_YEAR, -1);
            String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(cal.getTime());

            if (yesterday.equals(lastDate)) {
                xp += BONUS_STREAK_PER_DAY;
            }
        }

        return xp;
    }


    private void updateStreakCloud(
            int score,
            int xpEarned,
            String gameType,
            boolean isNewPb
    ) {
        if (firebaseUser == null) {
            // Not signed in: just do a local SharedPrefs update for streak & lastDailyDate:
            int newStreak = updateStreakLocallyOnly();
            // Now update the UI (with newStreak). We still display XP, high score, etc.
            displayResults(score, isNewPb, xpEarned, newStreak);
            return;
        }

        String uid = firebaseUser.getUid();
        DocumentReference userRef = firestore.collection("users").document(uid);

        // 1) Read remote values
        userRef.get()
                .addOnSuccessListener(snapshot -> {
                    // 2) Extract remote streak fields (if exist)
                    int remoteStreak = 0;
                    String remoteDate = "";

                    if (snapshot.exists()) {
                        if (snapshot.contains("currentStreak")) {
                            remoteStreak = snapshot.getLong("currentStreak").intValue();
                        }
                        if (snapshot.contains("lastDailyDate")) {
                            remoteDate = snapshot.getString("lastDailyDate");
                        }
                    }

                    // 3) Compute new streak based on remoteDate vs. today
                    Calendar cal = Calendar.getInstance();
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            .format(cal.getTime());

                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            .format(cal.getTime());

                    int updatedStreak;
                    if (yesterday.equals(remoteDate)) {
                        // Continuing streak
                        updatedStreak = remoteStreak + 1;
                    } else if (!today.equals(remoteDate)) {
                        // They skipped or first time
                        updatedStreak = 1;
                    } else {
                        // remoteDate == today → they somehow replayed same day (should not happen)
                        // Just leave updatedStreak = remoteStreak
                        updatedStreak = remoteStreak;
                    }

                    // 4) Build update map
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("lastDailyDate", today);
                    updates.put("currentStreak", updatedStreak);
                    updates.put("lastWordDashScore", score);

                    // Only change bestWordDashScore if isNewPb
                    if (isNewPb) {
                        updates.put("bestWordDashScore", score);
                    }
                    // always increment totalXP
                    updates.put("totalXP", FieldValue.increment(xpEarned));

                    // If they just hit a 7-day streak, award a trophy
                    if (updatedStreak == 7) {
                        updates.put("trophies", FieldValue.arrayUnion("7DayStreak"));
                    }

                    // 5) Merge into Firestore
                    userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                // 6) Mirror those two streak fields locally now that remote succeeded:
                                prefs.edit()
                                        .putString("last_played_date", today)
                                        .putInt("current_streak", updatedStreak)
                                        .apply();

                                // 7) Finally add a session entry to /sessions/...
                                writeSessionEntry(uid, score, xpEarned, gameType);

                                // 8) Now update UI with the newly computed streak
                                displayResults(score, isNewPb, xpEarned, updatedStreak);
                            })
                            .addOnFailureListener(e -> {
                                // If Firestore merge fails for some reason, still mirror locally:
                                prefs.edit()
                                        .putString("last_played_date", today)
                                        .putInt("current_streak", updatedStreak)
                                        .apply();

                                // Still write a local session entry if you want (or skip)
                                writeSessionEntry(uid, score, xpEarned, gameType);

                                // Update UI anyway
                                displayResults(score, isNewPb, xpEarned, updatedStreak);
                            });
                })
                .addOnFailureListener(e -> {
                    // If we cannot read remote at all, fallback to local-only
                    int fallbackStreak = updateStreakLocallyOnly();
                    displayResults(score, isNewPb, xpEarned, fallbackStreak);
                });
    }

    private int updateStreakLocallyOnly() {
        Calendar cal = Calendar.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(cal.getTime());

        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(cal.getTime());

        String lastDate = prefs.getString("last_played_date", "");
        int localStreak = prefs.getInt("current_streak", 0);

        int updatedStreak;
        if (yesterday.equals(lastDate)) {
            updatedStreak = localStreak + 1;
        } else if (!today.equals(lastDate)) {
            updatedStreak = 1;
        } else {
            updatedStreak = localStreak; // Already played today
        }

        prefs.edit()
                .putString("last_played_date", today)
                .putInt("current_streak", updatedStreak)
                .apply();

        return updatedStreak;
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

    private void syncStreakWithFirestore(
            String uid,
            int score,
            int xpEarned,
            String gameType,
            boolean isNewPb
    ) {
        DocumentReference userRef = firestore.collection("users").document(uid);

        userRef.get().addOnSuccessListener(snapshot -> {
            int remoteStreak = 0;
            String remoteDate = "";

            if (snapshot.exists()) {
                if (snapshot.contains("currentStreak")) {
                    remoteStreak = snapshot.getLong("currentStreak").intValue();
                }
                if (snapshot.contains("lastPlayedDate")) {
                    remoteDate = snapshot.getString("lastPlayedDate");
                }
            }

            // Compute “today” and “yesterday”
            Calendar cal = Calendar.getInstance();
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(cal.getTime());

            cal.add(Calendar.DAY_OF_YEAR, -1);
            String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(cal.getTime());

            int updatedStreak;
            if (yesterday.equals(remoteDate)) {
                // They played on “yesterday” (remote), so continue streak
                updatedStreak = remoteStreak + 1;
            } else if (!today.equals(remoteDate)) {
                // If remoteDate is anything else (blank or older), reset to 1
                updatedStreak = 1;
            } else {
                // remoteDate == today → they already played earlier today (rare),
                // so do not increment again in the same day
                updatedStreak = remoteStreak;
            }

            // Prepare the Firestore update map
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastPlayedDate", today);
            updates.put("currentStreak", updatedStreak);
            updates.put("lastGameScore", score);

            if (isNewPb) {
                updates.put("bestGameScore", score);
            }

            updates.put("totalXP", FieldValue.increment(xpEarned));

            // If they just hit exactly 7‐day streak, award the “7DayStreak” trophy
            if (updatedStreak == 7) {
                updates.put("trophies", FieldValue.arrayUnion("7DayStreak"));
            }

            // Merge into Firestore
            userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        // Mirror those two streak fields locally
                        prefs.edit()
                                .putString("last_played_date", today)
                                .putInt("current_streak", updatedStreak)
                                // Also update “total_xp” locally if desired:
                                .putInt("total_xp", prefs.getInt("total_xp", 0) + xpEarned)
                                .apply();

                        // Log a session entry
                        writeSessionEntry(uid, score, xpEarned, gameType);

                        // Finally update the UI:
                        displayResults(score, isNewPb, xpEarned, updatedStreak);
                    })
                    .addOnFailureListener(e -> {
                        // If Firestore merge fails, still do a local fallback
                        int fallbackStreak = updateStreakLocallyOnly();
                        prefs.edit()
                                .putInt("total_xp", prefs.getInt("total_xp", 0) + xpEarned)
                                .apply();
                        writeSessionEntry(uid, score, xpEarned, gameType);
                        displayResults(score, isNewPb, xpEarned, fallbackStreak);
                    });
        }).addOnFailureListener(e -> {
            // If get() fails entirely, fallback to strictly local
            int fallbackStreak = updateStreakLocallyOnly();
            prefs.edit()
                    .putInt("total_xp", prefs.getInt("total_xp", 0) + xpEarned)
                    .apply();
            writeSessionEntry(uid, score, xpEarned, gameType);
            displayResults(score, isNewPb, xpEarned, fallbackStreak);
        });
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
    
    private void setupButtons(String gameType) {
        playAgainButton.setOnClickListener(v -> {
                Intent gameIntent = new Intent(this, WordDashActivity.class);
                startActivity(gameIntent);
            finish();
        });
        
        homeButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            finish();
        });
    }
} 