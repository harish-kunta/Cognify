package com.gigamind.cognify;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.work.StreakNotificationScheduler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.gigamind.cognify.engine.DictionaryProvider;

/**
 * In onCreate():
 * 1) Initialize Firebase & preload dictionary.
 * 2) Attach a real-time Firestore listener so that SharedPreferences are immediately
 *    populated (from local cache) and then updated again when the server arrives.
 * 3) Only after that listener is in place do we schedule the daily “streak” Worker.
 */
public class CognifyApplication extends Application {
    private static final String TAG = "CognifyApplication";
    private UserRepository userRepo;
    private com.google.firebase.firestore.ListenerRegistration listenerRegistration;

    @Override
    public void onCreate() {
        super.onCreate();

        // (1) Initialize Firebase
        FirebaseApp.initializeApp(this);

        // (1b) Preload dictionary (so WordDashActivity doesn’t flash an empty grid)
        DictionaryProvider.preloadDictionary(this);

        userRepo = new UserRepository(getApplicationContext());

        // (2) If user is already signed in, attach a real-time listener
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            listenerRegistration = userRepo.attachUserDocumentListener(new UserRepository.OnUserDataChanged() {
                @Override
                public void onDataChanged() {
                    // At this point SharedPreferences hold correct “lastPlayedTimestamp” (and streak, xp, etc.)
                    Log.d(TAG, "Firestore → SharedPrefs listener fired. Scheduling Worker now.");
                    StreakNotificationScheduler.scheduleFromSharedPrefs(
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            getApplicationContext()
                    );

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
