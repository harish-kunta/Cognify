package com.gigamind.cognify.ui;

import android.media.MediaPlayer;
import android.os.Bundle;

import com.gigamind.cognify.ui.BaseActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.gigamind.cognify.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gigamind.cognify.analytics.GameAnalytics;
import com.gigamind.cognify.util.SoundManager;

public class MainActivity extends BaseActivity {
    private MediaPlayer buttonSound;

    private NavController navController;
    private BottomNavigationView bottomNavigation;

    private GameAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        analytics = GameAnalytics.getInstance(this);
        analytics.startSession();
        analytics.logScreenView("main_screen");

        setupNavigation();
    }

    private void setupNavigation() {
        // Find the NavHostFragment and get the NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Bind BottomNavigationView with NavController
        bottomNavigation = findViewById(R.id.bottomNavigation);
        if (navController != null) {
            NavigationUI.setupWithNavController(bottomNavigation, navController);
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            SoundManager.getInstance(this).playButton();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    analytics.logButtonClick("nav_home");
                    break;
                case R.id.navigation_leaderboard:
                    analytics.logButtonClick("nav_leaderboard");
                    break;
                case R.id.navigation_profile:
                    analytics.logButtonClick("nav_profile");
                    break;
                case R.id.navigation_settings:
                    analytics.logButtonClick("nav_settings");
                    break;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance(this).release();
        analytics.endSession();
    }
}