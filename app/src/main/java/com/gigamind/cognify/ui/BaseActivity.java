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
    }

    /**
     * Ensures the entire activity tree has system click sounds disabled.
     * This needs to be done *after* the content view is attached so that
     * newly inflated views do not re-enable the default sound effects.
     */
    private void disableSystemSounds() {
        getWindow().getDecorView().setSoundEffectsEnabled(false);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        disableSystemSounds();
    }

    @Override
    public void setContentView(android.view.View view) {
        super.setContentView(view);
        disableSystemSounds();
    }

    @Override
    public void setContentView(android.view.View view,
            android.view.ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        disableSystemSounds();
    }
}
