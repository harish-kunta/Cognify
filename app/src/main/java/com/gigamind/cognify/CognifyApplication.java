package com.gigamind.cognify;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.gigamind.cognify.util.ExceptionLogger;

import androidx.annotation.NonNull;

import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.util.Constants;
import com.gigamind.cognify.work.StreakNotificationScheduler;
import com.gigamind.cognify.work.QuestNotificationScheduler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.gigamind.cognify.engine.DictionaryProvider;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.gigamind.cognify.BuildConfig;
import android.content.SharedPreferences;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * In onCreate():
 * 1) Initialize Firebase & preload dictionary.
 * 2) Attach a real-time Firestore listener so that SharedPreferences are immediately
 *    populated (from local cache) and then updated again when the server arrives.
 * 3) Only after that listener is in place do we schedule the daily "streak" Worker.
 */
public class CognifyApplication extends Application {
    private static final String TAG = "CognifyApplication";
    private UserRepository userRepo;
    private com.google.firebase.firestore.ListenerRegistration listenerRegistration;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences themePrefs = getSharedPreferences(Constants.PREF_APP, MODE_PRIVATE);
        boolean darkMode = themePrefs.getBoolean(Constants.PREF_DARK_MODE_ENABLED, false);
        AppCompatDelegate.setDefaultNightMode(darkMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        Thread.UncaughtExceptionHandler previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            ExceptionLogger.log(TAG, e);
            if (previousHandler != null) {
                previousHandler.uncaughtException(t, e);
            }
        });

        // (1) Initialize Firebase
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance());
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance());
        }
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // (1b) Preload dictionary (so WordDashActivity doesn't flash an empty grid)
        DictionaryProvider.preloadDictionary(this);

        userRepo = new UserRepository(getApplicationContext());

        // Check notification permission status
        checkNotificationPermission();

        // (2) If user is already signed in, attach a real-time listener
        if (FirebaseService.getInstance().isUserSignedIn()) {
            listenerRegistration = userRepo.attachUserDocumentListener(new UserRepository.OnUserDataChanged() {
                @Override
                public void onDataChanged() {
                    // At this point SharedPreferences hold correct "lastPlayedTimestamp" (and streak, xp, etc.)
                    Log.d(TAG, "Firestore → SharedPrefs listener fired. Scheduling Worker now.");
                    
                    // Only schedule if notifications are enabled
                    SharedPreferences prefs = getSharedPreferences(Constants.PREF_NOTIFICATION, MODE_PRIVATE);
                    if (prefs.getBoolean(Constants.PREF_NOTIFICATION_ENABLED, true)) {
                        StreakNotificationScheduler.scheduleFromSharedPrefs(
                                FirebaseService.getInstance().getCurrentUserId(),
                                getApplicationContext()
                        );
                        QuestNotificationScheduler.scheduleDaily(getApplicationContext());
                    }

                    // We only need to schedule once on cold start; after that the Worker will
                    // re-schedule itself. So remove the listener right away:
                    if (listenerRegistration != null) {
                        listenerRegistration.remove();
                        listenerRegistration = null;
                    }
                }
            });
        } else {
            // (3) Not signed in → schedule the Worker immediately from whatever prefs we already have
            StreakNotificationScheduler.scheduleFromSharedPrefs(/*firebaseUid=*/ null,
                    getApplicationContext());
            QuestNotificationScheduler.scheduleDaily(getApplicationContext());
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, save this state
                getSharedPreferences(Constants.PREF_NOTIFICATION, MODE_PRIVATE)
                    .edit()
                    .putBoolean(Constants.PREF_NOTIFICATION_ENABLED, false)
                    .apply();
                Log.d(TAG, "Notification permission not granted on Android 13+");
            } else {
                // Permission granted
                getSharedPreferences(Constants.PREF_NOTIFICATION, MODE_PRIVATE)
                    .edit()
                    .putBoolean(Constants.PREF_NOTIFICATION_ENABLED, true)
                    .apply();
                Log.d(TAG, "Notification permission granted on Android 13+");
            }
        } else {
            // For Android 12 and below, notifications are enabled by default
            getSharedPreferences(Constants.PREF_NOTIFICATION, MODE_PRIVATE)
                .edit()
                .putBoolean(Constants.PREF_NOTIFICATION_ENABLED, true)
                .apply();
            Log.d(TAG, "Notification enabled by default (Android 12 or below)");
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Clean up listener if still active (unlikely on normal Android lifecycles, but safe)
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }
}
