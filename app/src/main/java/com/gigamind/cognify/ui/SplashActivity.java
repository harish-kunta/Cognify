package com.gigamind.cognify.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.gigamind.cognify.databinding.ActivitySplashBinding;
import com.gigamind.cognify.util.Constants;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private static final long SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Disable heavy animations on low memory devices
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null && am.isLowRamDevice()) {
            binding.splashAnimation.cancelAnimation();
            binding.splashAnimation.setVisibility(View.GONE);
        }

        // Check if first time launch
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_APP, MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean(Constants.PREF_IS_FIRST_LAUNCH, true);

        // Deep link handling for challenge
        android.net.Uri data = getIntent().getData();
        if (data != null && "/challenge".equals(data.getPath())) {
            String type = data.getQueryParameter("type");
            String scoreStr = data.getQueryParameter("score");
            Intent gameIntent = null;
            if ("word".equals(type)) {
                gameIntent = new Intent(this, WordDashActivity.class);
            } else if ("math".equals(type)) {
                gameIntent = new Intent(this, QuickMathActivity.class);
            }
            if (gameIntent != null) {
                if (scoreStr != null) {
                    try {
                        int s = Integer.parseInt(scoreStr);
                        gameIntent.putExtra(Constants.EXTRA_CHALLENGE_SCORE, s);
                    } catch (NumberFormatException ignored) {}
                }
                gameIntent.putExtra(Constants.EXTRA_CHALLENGE_TYPE, type);
                startActivity(gameIntent);
                finish();
                return;
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (isFirstLaunch) {
                // First time launch - go to onboarding
                intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                // Set first launch to false
                prefs.edit().putBoolean(Constants.PREF_IS_FIRST_LAUNCH, false).apply();
            } else {
                // Regular launch - go to main activity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
} 