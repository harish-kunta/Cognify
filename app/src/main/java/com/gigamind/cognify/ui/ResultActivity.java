package com.gigamind.cognify.ui;

import static com.gigamind.cognify.data.repository.UserRepository.KEY_LAST_PLAYED_DATE;
import static com.gigamind.cognify.data.repository.UserRepository.KEY_LAST_PLAYED_TS;
import static com.gigamind.cognify.data.repository.UserRepository.KEY_CURRENT_STREAK;
import static com.gigamind.cognify.data.repository.UserRepository.KEY_PERSONAL_BEST_XP;
import static com.gigamind.cognify.data.repository.UserRepository.KEY_TOTAL_XP;
import static com.gigamind.cognify.util.Constants.BONUS_NEW_PB;
import static com.gigamind.cognify.util.Constants.BONUS_STREAK_PER_DAY;
import static com.gigamind.cognify.util.Constants.INTENT_FOUND_WORDS;
import static com.gigamind.cognify.util.Constants.INTENT_SCORE;
import static com.gigamind.cognify.util.Constants.INTENT_TYPE;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import com.gigamind.cognify.util.Constants;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.gigamind.cognify.R;
import com.gigamind.cognify.analytics.GameAnalytics;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.util.AnimationUtils;
import com.gigamind.cognify.work.StreakNotificationScheduler;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class ResultActivity extends AppCompatActivity {

    private TextView headerText;
    private TextView scoreValue, totalXPValue, totalWordText, highScoreText, streakText;
    private TextView newHighScoreText, encouragementText;
    private MaterialButton playAgainButton, homeButton;
    private LinearLayout playContainer;
    private LottieAnimationView confettiView;
    private static final String[] ENCOURAGEMENTS = {
            "Amazing!", "Unstoppable!", "You nailed it!", "Keep it going!", "🔥 Hot streak!"
    };

    private SharedPreferences prefs;
    private UserRepository userRepository;
    private FirebaseUser firebaseUser;
    private MediaPlayer dingSound;
    private GameAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        analytics = GameAnalytics.getInstance(this);
        analytics.logScreenView(Constants.ANALYTICS_SCREEN_RESULT);

        initializeViews();

        // Initialize MediaPlayer only if it hasn't been created yet
        if (dingSound == null) {
            dingSound = MediaPlayer.create(this, R.raw.lesson_complete);
        }

        prefs          = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userRepository = new UserRepository(this);
        firebaseUser   = FirebaseService.getInstance().getCurrentUser();

        int score        = getIntent().getIntExtra(INTENT_SCORE, 0);
        String gameType  = getIntent().getStringExtra(INTENT_TYPE);
        int wordsFound   = getIntent().getIntExtra(INTENT_FOUND_WORDS, 0);

        // (1) Compute how much XP was earned (PB + streak bonus)
        int xpEarned = calculateXpEarned(score, gameType);

        // (2) Update local high‐score synchronously
        boolean isNewPb = updateHighScoreLocal(score, gameType);

        Integer newPbValue = null;
        if (isNewPb) {
            newPbValue = prefs.getInt(KEY_PERSONAL_BEST_XP, 0);
        }

        // (3) Read "old" streak / XP from prefs (already kept in sync elsewhere)
        String oldDate    = prefs.getString(KEY_LAST_PLAYED_DATE, "");
        int    oldStreak  = prefs.getInt(KEY_CURRENT_STREAK, 0);
        int    oldTotalXp = prefs.getInt(KEY_TOTAL_XP, 0);

        // (4) Compute "today" / "yesterday"
        Calendar nowCal = Calendar.getInstance();
        String todayStr    = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(nowCal.getTime());
        nowCal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(nowCal.getTime());

        // (5) Compute newStreak locally:
        int newStreak;
        if (yesterdayStr.equals(oldDate)) {
            newStreak = oldStreak + 1;
        } else if (!todayStr.equals(oldDate)) {
            newStreak = 1;
        } else {
            newStreak = oldStreak;
        }

        // (6) Compute newTotalXp:
        int newTotalXp = oldTotalXp + xpEarned;

        // (7) Write new values into SharedPreferences:
        long nowMillis = System.currentTimeMillis();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LAST_PLAYED_DATE, todayStr);
        editor.putLong(KEY_LAST_PLAYED_TS, nowMillis);
        editor.putInt(KEY_CURRENT_STREAK, newStreak);
        editor.putInt(KEY_TOTAL_XP, newTotalXp);

        // Only overwrite personal‐best if this run was a new PB
        if (isNewPb) {
            editor.putInt(KEY_PERSONAL_BEST_XP, newPbValue);
        }

        editor.apply();

        // (8) Kick off Firestore merge in background
        Task<Void> updateTask = userRepository.updateGameResults(
                gameType, score, xpEarned, newPbValue
        );

        // (9) Schedule next streak notification (uses prefs key we just wrote)
        String uid = (firebaseUser != null) ? firebaseUser.getUid() : null;
        StreakNotificationScheduler.scheduleFromSharedPrefs(uid, this);

        // (10) Animate everything in sequence:
        animateHeader();
        animateNumbersSequentially(
                score, xpEarned, newTotalXp, newStreak, isNewPb, (updateTask != null), wordsFound
        );

        setupButtons(gameType);
    }

    private void initializeViews() {
        headerText       = findViewById(R.id.headerText);
        scoreValue       = findViewById(R.id.scoreValue);
        totalXPValue     = findViewById(R.id.totalXPValue);
        totalWordText    = findViewById(R.id.totalWordText);
        highScoreText    = findViewById(R.id.highScoreText);
        streakText       = findViewById(R.id.streakText);
        newHighScoreText = findViewById(R.id.newHighScoreText);
        encouragementText= findViewById(R.id.encouragementText);
        playAgainButton  = findViewById(R.id.playAgainButton);
        homeButton       = findViewById(R.id.homeButton);
        playContainer    = (LinearLayout) playAgainButton.getParent();
        confettiView     = findViewById(R.id.confettiView);

        // Hide everything initially
        headerText.setAlpha(0f);
        scoreValue.setText("0");
        totalXPValue.setText("0");
        totalWordText.setText("0");
        streakText.setAlpha(0f);
        newHighScoreText.setAlpha(0f);
        playContainer.setAlpha(0f);
        encouragementText.setAlpha(0f);
    }

    private int calculateXpEarned(int score, String gameType) {
        int xp = score;

        int existingPb = prefs.getInt(KEY_PERSONAL_BEST_XP, 0);
        if (score > existingPb) {
            xp += BONUS_NEW_PB;
        }

        // (2) Streak bonus if lastPlayedDate (in prefs) was yesterday
        String lastDate = prefs.getString(KEY_LAST_PLAYED_DATE, "");
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

    private boolean updateHighScoreLocal(int score, String gameType) {
        String highScoreKey = KEY_PERSONAL_BEST_XP;
        int currentHigh     = prefs.getInt(highScoreKey, 0);

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

    private void animateHeader() {
        // Fade headerText in over 400ms
        AnimationUtils.fadeIn(headerText, 400);
    }

    private void animateNumbersSequentially(
            final int finalScore,
            final int xpGained,
            final int finalTotalXp,
            final int finalStreak,
            final boolean isNewPb,
            final boolean willSyncRemote,
            final int wordsFound
    ) {
        // 1) Score count‐up
        ValueAnimator scoreAnim = ValueAnimator.ofInt(0, finalScore);
        scoreAnim.setDuration(600);
        scoreAnim.addUpdateListener(anim -> {
            int val = (Integer) anim.getAnimatedValue();
            scoreValue.setText(String.valueOf(val));
        });
        scoreAnim.start();

        // 2) After score animation finishes → animate words found (totalWordText)
        scoreAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Ding sound on XP reveal:
                if (xpGained > 0 && dingSound != null) dingSound.start();
                AnimationUtils.fadeIn(totalWordText, 300);
                ValueAnimator wordsAnim = ValueAnimator.ofInt(0, wordsFound);
                wordsAnim.setDuration(500);
                wordsAnim.addUpdateListener(anim2 -> {
                    int val2 = (Integer) anim2.getAnimatedValue();
                    totalWordText.setText(String.valueOf(val2));
                });
                wordsAnim.start();

                wordsAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // 3) Animate Total XP
                        int startXp = prefs.getInt(KEY_TOTAL_XP, 0) - xpGained;
                        ValueAnimator xpAnim = ValueAnimator.ofInt(startXp, finalTotalXp);
                        xpAnim.setDuration(600);
                        xpAnim.addUpdateListener(anim3 -> {
                            int val3 = (Integer) anim3.getAnimatedValue();
                            totalXPValue.setText(String.valueOf(val3));
                        });
                        xpAnim.start();

                        xpAnim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // 4) Animate Streak
                                streakText.setText("🔥 " + finalStreak + " Day Streak");
                                AnimationUtils.fadeIn(streakText, 400);

                                if (isNewPb) {
                                    animateNewHighScoreBanner();
                                }

                                // 6) Show encouragement text (e.g. "Amazing!")
                                showEncouragement(randomEncouragement());

                                // 2) Fire off confetti
                                confettiView.setVisibility(View.VISIBLE);
                                confettiView.playAnimation();

                                // 7) Finally, fade in Play/Back buttons after 800ms
                                AnimationUtils.fadeInWithDelay(playContainer, 800, 400);
                            }
                        });
                    }
                });
            }
        });
    }

    private void animateNewHighScoreBanner() {
        // 1) Pulse the "New High Score!" text
        newHighScoreText.setAlpha(0f);
        newHighScoreText.setScaleX(0.5f);
        newHighScoreText.setScaleY(0.5f);
        newHighScoreText.setVisibility(View.VISIBLE);

        // Fade in first
        AnimationUtils.fadeIn(newHighScoreText, 500);

        // Then pulse
        new Handler().postDelayed(() ->
                        AnimationUtils.pulse(newHighScoreText, 1.1f, 300),
                500
        );
    }

    private void showEncouragement(String message) {
        // Apply glow shader
        Shader myShader = new LinearGradient(
                0, 100, 0, 100,
                Color.parseColor("#f7e307"), Color.parseColor("#fda200"),
                Shader.TileMode.CLAMP
        );
        encouragementText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        encouragementText.getPaint().setShader(myShader);
        encouragementText.setText(message.toUpperCase());

        AnimationUtils.fadeIn(encouragementText, 400);
    }

    private String randomEncouragement() {
        int idx = new Random().nextInt(ENCOURAGEMENTS.length);
        return ENCOURAGEMENTS[idx];
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

    @Override
    protected void onStop() {
        super.onStop();
        // Release MediaPlayer in onStop to handle configuration changes
        if (dingSound != null) {
            dingSound.release();
            dingSound = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure MediaPlayer is released
        if (dingSound != null) {
            dingSound.release();
            dingSound = null;
        }
    }

    // A tiny AnimatorListenerAdapter to avoid boilerplate
    private abstract static class AnimatorListenerAdapter implements Animator.AnimatorListener {
        @Override public void onAnimationStart(Animator animation) {}
        @Override public void onAnimationCancel(Animator animation) {}
        @Override public void onAnimationRepeat(Animator animation) {}
        @Override public void onAnimationEnd(Animator animation) {}
    }
}
