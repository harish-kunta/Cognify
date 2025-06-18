package com.gigamind.cognify.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Base activity that disables default system click sounds.
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Disable system provided sound effects for all views so only custom
        // sounds play when enabled in preferences.
        getWindow().getDecorView().setSoundEffectsEnabled(false);
    }
}
