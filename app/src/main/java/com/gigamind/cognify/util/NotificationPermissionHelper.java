package com.gigamind.cognify.util;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

/**
 * Helper that encapsulates the logic for requesting the POST_NOTIFICATIONS
 * permission on Android 13+ devices. This keeps {@link com.gigamind.cognify.ui.OnboardingActivity}
 * focused on UI logic.
 */
public class NotificationPermissionHelper {

    /** Callback for permission result. */
    public interface PermissionCallback {
        void onPermissionResult(boolean granted);
    }

    private final ComponentActivity activity;
    private final SharedPreferences prefs;
    private final ActivityResultLauncher<String> launcher;
    private final PermissionCallback callback;

    public NotificationPermissionHelper(
            @NonNull ComponentActivity activity,
            @NonNull SharedPreferences prefs,
            @NonNull PermissionCallback callback) {
        this.activity = activity;
        this.prefs = prefs;
        this.callback = callback;

        this.launcher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        String msg = "Notifications enabled. You won't lose your streak!";
                        View root = activity.findViewById(android.R.id.content);
                        Snackbar.make(root, msg, Snackbar.LENGTH_SHORT).show();
                        root.announceForAccessibility(msg);
                    } else {
                        prefs.edit()
                                .putBoolean(Constants.PREF_ASKED_NOTIFICATIONS, true)
                                .apply();
                        String msg = "Notifications disabled. You may lose your streak if you don't play.";
                        View root = activity.findViewById(android.R.id.content);
                        Snackbar.make(root, msg, Snackbar.LENGTH_SHORT).show();
                        root.announceForAccessibility(msg);
                    }
                    callback.onPermissionResult(isGranted);
                }
        );
    }
    /**
     * Checks whether the permission should be requested and, if so,
     * presents a rationale dialog before launching the system prompt.
     */
    public void requestIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return; // Permission not required on older versions
        }
        boolean alreadyAsked = prefs.getBoolean(Constants.PREF_ASKED_NOTIFICATIONS, false);
        if (alreadyAsked) {
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return; // Already granted
        }
        new AlertDialog.Builder(activity)
                .setTitle("Keep Your Streak Alive!")
                .setMessage("We'd like to send you a daily reminder so you won't lose your hard-earned streak. " +
                        "Allow notifications to receive gentle nudges if you haven't played today.")
                .setPositiveButton("Enable", (dialog, which) ->
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS))
                .setNegativeButton("No thanks", (dialog, which) -> {
                    prefs.edit()
                            .putBoolean(Constants.PREF_ASKED_NOTIFICATIONS, true)
                            .apply();
                    dialog.dismiss();
                    callback.onPermissionResult(false);
                })
                .setCancelable(false)
                .show();
    }
}
