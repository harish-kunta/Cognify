package com.gigamind.cognify.ui;

import static com.gigamind.cognify.data.repository.UserRepository.KEY_LAST_PLAYED_DATE;
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
import com.gigamind.cognify.data.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private TextView scoreText, streakText, highScoreText, newHighScoreText, xpGainedText, totalXPText;

    private MaterialButton playAgainButton;
    private MaterialButton homeButton;
    private SharedPreferences prefs;
    private UserRepository userRepository;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Initialize views
        initializeViews();

        prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        userRepository = new UserRepository(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        int score = getIntent().getIntExtra(INTENT_SCORE, 0);
        String gameType = getIntent().getStringExtra(INTENT_TYPE);

        int xpEarned = calculateXpEarned(score, gameType);
        boolean isNewPb = updateHighScoreLocal(score, gameType);

        userRepository.updateGameResults(gameType, score, xpEarned);

        final int updatedStreak = userRepository.getCurrentStreak();
        final int updatedTotalXp = userRepository.getTotalXP();

        displayResults(score, isNewPb, xpEarned, updatedStreak, updatedTotalXp);
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

        // Streak bonus if user played yesterday (as per SharedPrefs)
        String lastDate = prefs.getString(KEY_LAST_PLAYED_DATE, "");
        if (!lastDate.isEmpty()) {
            Calendar cal = Calendar.getInstance();
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());

            cal.add(Calendar.DAY_OF_YEAR, -1);
            String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());

            if (yesterday.equals(lastDate)) {
                xp += BONUS_STREAK_PER_DAY;
            }
        }

        return xp;
    }

    private void initializeViews() {
        scoreText        = findViewById(R.id.scoreValue);
        streakText       = findViewById(R.id.streakText);
        highScoreText    = findViewById(R.id.highScoreText);
        newHighScoreText = findViewById(R.id.newHighScoreText);
        xpGainedText     = findViewById(R.id.xpGainedText);
        totalXPText      = findViewById(R.id.totalXPValue);

        playAgainButton  = findViewById(R.id.playAgainButton);
        homeButton       = findViewById(R.id.homeButton);
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

    private void displayResults(int score,
                                boolean isNewPb,
                                int xpGained,
                                int currentStreak,
                                int totalXp) {
        scoreText.setText(String.valueOf(score));
        xpGainedText.setText(String.valueOf(xpGained));
        totalXPText.setText(String.valueOf(totalXp));
        streakText.setText(String.valueOf(currentStreak));
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
