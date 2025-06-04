package com.gigamind.cognify.ui;

import static com.gigamind.cognify.data.repository.UserRepository.KEY_LAST_PLAYED_DATE;
import static com.gigamind.cognify.data.repository.UserRepository.KEY_LAST_PLAYED_TS;
import static com.gigamind.cognify.data.repository.UserRepository.KEY_CURRENT_STREAK;
import static com.gigamind.cognify.data.repository.UserRepository.KEY_PERSONAL_BEST_XP;
import static com.gigamind.cognify.data.repository.UserRepository.KEY_TOTAL_XP;
import static com.gigamind.cognify.util.Constants.BONUS_NEW_PB;
import static com.gigamind.cognify.util.Constants.BONUS_STREAK_PER_DAY;
import static com.gigamind.cognify.util.Constants.INTENT_SCORE;
import static com.gigamind.cognify.util.Constants.INTENT_TYPE;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.gigamind.cognify.R;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.work.StreakNotificationScheduler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initializeViews();

        // Optional: a “ding” sound for big wins
        dingSound = MediaPlayer.create(this, R.raw.success_sound);

        prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        userRepository = new UserRepository(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        int score = getIntent().getIntExtra(INTENT_SCORE, 0);
        String gameType = getIntent().getStringExtra(INTENT_TYPE);

        // 1) Compute how much XP was earned (PB + streak bonus)
        int xpEarned = calculateXpEarned(score, gameType);

        // 2) Update local high‐score synchronously
        boolean isNewPb = updateHighScoreLocal(score, gameType);

        int newPbValue  = -1;
        if (isNewPb) {
            // We just wrote a new “pb_worddash” into SharedPrefs; read it back:
            newPbValue = prefs.getInt(KEY_PERSONAL_BEST_XP, 0);
        }

        // 3) Read “old” streak / XP from prefs (already kept in sync elsewhere)
        String oldDate = prefs.getString(KEY_LAST_PLAYED_DATE, "");
        int oldStreak = prefs.getInt(KEY_CURRENT_STREAK, 0);
        int oldTotalXp = prefs.getInt(KEY_TOTAL_XP, 0);

        // 4) Compute “today” / “yesterday”
        Calendar nowCal = Calendar.getInstance();
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(nowCal.getTime());
        nowCal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(nowCal.getTime());

        // 5) Compute newStreak locally:
        int newStreak;
        if (yesterdayStr.equals(oldDate)) {
            newStreak = oldStreak + 1;
        } else if (!todayStr.equals(oldDate)) {
            newStreak = 1;
        } else {
            newStreak = oldStreak;
        }

        // 6) Compute newTotalXp:
        int newTotalXp = oldTotalXp + xpEarned;

        // 7) Immediately write those new values into SharedPreferences:
        long nowMillis = System.currentTimeMillis();
        prefs.edit()
                .putString(KEY_LAST_PLAYED_DATE, todayStr)
                .putLong(KEY_LAST_PLAYED_TS, nowMillis)
                .putInt(KEY_CURRENT_STREAK, newStreak)
                .putInt(KEY_TOTAL_XP, newTotalXp)
                .putInt(KEY_PERSONAL_BEST_XP, newPbValue)
                .apply();

        // 8) Kick off background Firestore merge (we don’t block UI on this)
        Task<Void> updateTask = userRepository.updateGameResults(gameType, score, xpEarned, newPbValue);

        // 9) Schedule next streak notification
        String uid = (firebaseUser != null) ? firebaseUser.getUid() : null;
        StreakNotificationScheduler.scheduleFromSharedPrefs(uid, this);

        // 10) Animate everything in sequence:
        animateHeader();
        animateNumbersSequentially(score, xpEarned, newTotalXp, newStreak, isNewPb, updateTask != null);

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

    private String randomEncouragement() {
        int idx = new Random().nextInt(ENCOURAGEMENTS.length);
        return ENCOURAGEMENTS[idx];
    }

    private int calculateXpEarned(int score, String gameType) {
        int xp = score;

        // Personal best bonus:
        String pbKey = "pb_" + gameType.toLowerCase();
        int existingPb = prefs.getInt(pbKey, 0);
        if (score > existingPb) {
            xp += BONUS_NEW_PB;
        }

        // Streak bonus if lastPlayedDate (in prefs) was yesterday:
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

    private boolean updateHighScoreLocal(int score, String gameType) {
        String highScoreKey = KEY_PERSONAL_BEST_XP;
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

    private void animateHeader() {
        headerText.animate()
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateNumbersSequentially(
            final int finalScore,
            final int xpGained,
            final int finalTotalXp,
            final int finalStreak,
            final boolean isNewPb,
            final boolean willSyncRemote) {

        // 1) Score count‐up
        ValueAnimator scoreAnim = ValueAnimator.ofInt(0, finalScore);
        scoreAnim.setDuration(600);
        scoreAnim.addUpdateListener(anim -> {
            int val = (Integer) anim.getAnimatedValue();
            scoreValue.setText(String.valueOf(val));
        });
        scoreAnim.start();


        // 2) After score animation finishes, animate XP Gained
        scoreAnim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                // Play a little “ding” on XP reveal
                if (xpGained > 0 && dingSound != null) dingSound.start();

                ValueAnimator xpAnim = ValueAnimator.ofInt(0, xpGained);
                xpAnim.setDuration(500);
                xpAnim.addUpdateListener(anim -> {
                    int val = (Integer) anim.getAnimatedValue();
                });
                xpAnim.start();

                xpAnim.addListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // 3) Animate Total XP
                        ValueAnimator totalXpAnim =
                                ValueAnimator.ofInt(prefs.getInt(KEY_TOTAL_XP, 0) - xpGained,
                                        finalTotalXp);
                        totalXpAnim.setDuration(600);
                        totalXpAnim.addUpdateListener(anim2 -> {
                            int val2 = (Integer) anim2.getAnimatedValue();
                            totalXPValue.setText(String.valueOf(val2));
                        });
                        totalXpAnim.start();

                        totalXpAnim.addListener(new SimpleAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // 4) Animate Streak
                                streakText.setText("🔥 " + finalStreak + " Day Streak");
                                streakText.animate()
                                        .alpha(1f)
                                        .setDuration(400)
                                        .setInterpolator(new AccelerateDecelerateInterpolator())
                                        .start();

                                // 5) If new PB, pulse that banner and launch confetti
                                if (isNewPb) {
                                    animateNewHighScoreBanner();
                                }

                                // 6) Show a brief encouraging phrase in the center
                                showEncouragement(randomEncouragement());

                                // 7) Finally, fade in the “Play Again” & “Back to Home” buttons
                                new Handler().postDelayed(() -> {
                                    playContainer.animate()
                                            .alpha(1f)
                                            .setDuration(400)
                                            .start();
                                }, 800);
                            }
                        });
                    }
                });
            }
        });
    }

    private void animateNewHighScoreBanner() {
        // 1) Fade in “New High Score!” text with a bounce
        newHighScoreText.setAlpha(0f);
        newHighScoreText.setScaleX(0.5f);
        newHighScoreText.setScaleY(0.5f);
        newHighScoreText.setVisibility(View.VISIBLE);
        newHighScoreText.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    // After it’s fully visible, pulse a few times
                    ScaleAnimation pulse = new ScaleAnimation(
                            1f, 1.1f, 1f, 1.1f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    pulse.setDuration(300);
                    pulse.setRepeatCount(2);
                    pulse.setRepeatMode(Animation.REVERSE);
                    newHighScoreText.startAnimation(pulse);
                })
                .start();

        // 2) Fire off confetti
        confettiView.setVisibility(View.VISIBLE);
        confettiView.playAnimation();

        // Hide confetti after ~2 seconds
        new Handler().postDelayed(() -> {
            confettiView.cancelAnimation();
            confettiView.setVisibility(View.GONE);
        }, 2000);
    }

    private void showEncouragement(String message) {
        Shader myShader = new LinearGradient(
                0, 100, 0, 100,
                Color.parseColor("#f7e307"), Color.parseColor("#fda200"),
                Shader.TileMode.CLAMP);
        // 1) Enable software rendering for BlurMaskFilter to work correctly:
        encouragementText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

// 2) Grab the TextView’s Paint and attach a BlurMaskFilter for the glow
        float radius = 32f;  // how “big” the glow is (increase for a softer halo)
        int glowColor = Color.argb(150, 255, 255, 0); // semi‐transparent yellow glow

//        encouragementText.getPaint().setColor(glowColor);
        encouragementText.getPaint().setShader( myShader );
        encouragementText.setText(message.toUpperCase());
        encouragementText.setAlpha(0f);
        encouragementText.animate()
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    // Utility listener to avoid boilerplate
    private abstract static class SimpleAnimatorListener implements Animator.AnimatorListener {
        @Override public void onAnimationStart(Animator animation) {}
        @Override public void onAnimationCancel(Animator animation) {}
        @Override public void onAnimationRepeat(Animator animation) {}
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
    protected void onDestroy() {
        super.onDestroy();
        if (dingSound != null) {
            dingSound.release();
            dingSound = null;
        }
    }
}
