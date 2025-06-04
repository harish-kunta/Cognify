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

public class CognifyApplication extends Application {
    private static final String TAG = "CognifyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // (1) Initialize Firebase if not already done
        FirebaseApp.initializeApp(this);

        UserRepository repo = new UserRepository(getApplicationContext());
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // Signed in → re‐sync Firestore → SharedPrefs, then schedule notification
            repo.syncUserData()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            Log.d(TAG, "Finished syncing Firestore → SharedPrefs.");
                            // Now we have correct lastPlayedTimestamp locally:
                            StreakNotificationScheduler.scheduleFromSharedPrefs(
                                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    getApplicationContext()
                            );
                        }
                    });
        } else {
            // Not signed in → simply schedule from whatever is already in SharedPrefs
            StreakNotificationScheduler.scheduleFromSharedPrefs(/*firebaseUid=*/ null,
                    getApplicationContext());
        }
    }
}

