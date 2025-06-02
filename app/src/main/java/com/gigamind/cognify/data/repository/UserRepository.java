package com.gigamind.cognify.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.gigamind.cognify.data.firebase.FirebaseService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserRepository {
    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_LAST_PLAYED_DATE = "last_played_date";
    private static final String KEY_CURRENT_STREAK = "current_streak";
    private static final String KEY_TOTAL_XP = "total_xp";

    private final SharedPreferences prefs;
    private final FirebaseService firebaseService;

    public UserRepository(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.firebaseService = FirebaseService.getInstance();
    }

    public Task<DocumentSnapshot> syncUserData() {
        if (!firebaseService.isUserSignedIn()) {
            return null;
        }

        return firebaseService.getUserDocument().get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Sync remote data to local
                        SharedPreferences.Editor editor = prefs.edit();
                        
                        if (snapshot.contains("currentStreak")) {
                            editor.putInt(KEY_CURRENT_STREAK, snapshot.getLong("currentStreak").intValue());
                        }
                        
                        if (snapshot.contains("lastPlayedDate")) {
                            editor.putString(KEY_LAST_PLAYED_DATE, snapshot.getString("lastPlayedDate"));
                        }
                        
                        if (snapshot.contains("totalXP")) {
                            editor.putInt(KEY_TOTAL_XP, snapshot.getLong("totalXP").intValue());
                        }
                        
                        editor.apply();
                    }
                });
    }

    public Task<Void> updateGameResults(String gameType, int score, int xpEarned) {
        // 1) Compute “today” and “yesterday” strings in UTC format
        Calendar now = Calendar.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(now.getTime());

        now.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(now.getTime());

        // 2) Read local lastPlayedDate/currentStreak (they should match Firestore if syncUserData() was called)
        String lastPlayedDate = prefs.getString(KEY_LAST_PLAYED_DATE, "");
        int currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0);

        int updatedStreak;
        if (yesterday.equals(lastPlayedDate)) {
            // Consecutive day
            updatedStreak = currentStreak + 1;
        } else if (!today.equals(lastPlayedDate)) {
            // Missed at least one day (or first play ever)
            updatedStreak = 1;
        } else {
            // They already played “today” once
            updatedStreak = currentStreak;
        }

        // 3) Update local SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LAST_PLAYED_DATE, today);
        editor.putInt(KEY_CURRENT_STREAK, updatedStreak);
        editor.putInt(KEY_TOTAL_XP, prefs.getInt(KEY_TOTAL_XP, 0) + xpEarned);
        editor.apply();

        // 4) If user is signed in, merge these changes into Firestore
        if (firebaseService.isUserSignedIn()) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastPlayedDate", today);
            updates.put("currentStreak", updatedStreak);
            // We use increment() so Firestore’s totalXP remains cumulative
            updates.put("totalXP", FieldValue.increment(xpEarned));
            updates.put("last" + gameType + "Score", score);

            if (updatedStreak == 7) {
                updates.put("trophies", FieldValue.arrayUnion("7DayStreak"));
            }

            return firebaseService.getUserDocument()
                    .set(updates, SetOptions.merge())
                    .continueWithTask(task ->
                            firebaseService.logGameSession(gameType, score, xpEarned)
                    );
        }

        return null;
    }

    public int getCurrentStreak() {
        return prefs.getInt(KEY_CURRENT_STREAK, 0);
    }

    public int getTotalXP() {
        return prefs.getInt(KEY_TOTAL_XP, 0);
    }

    public String getLastPlayedDate() {
        return prefs.getString(KEY_LAST_PLAYED_DATE, "");
    }
} 