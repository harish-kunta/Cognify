package com.gigamind.cognify.ui;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.gigamind.cognify.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gigamind.cognify.analytics.GameAnalytics;

public class MainActivity extends AppCompatActivity {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (buttonSound != null) {
            buttonSound.release();
            buttonSound = null;
        }
        analytics.endSession();
    }
}