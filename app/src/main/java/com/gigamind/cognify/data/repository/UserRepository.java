package com.gigamind.cognify.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.util.UserFields;
import com.gigamind.cognify.work.StreakNotificationScheduler;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * UserRepository now syncs “lastPlayedDate” (String) and “lastPlayedTimestamp” (millis)
 * between SharedPreferences and Firestore.  Cross-device streak logic works as long as Firestore is up-to-date.
 */
public class UserRepository {
    private static final String PREFS_NAME = "GamePrefs";

    // SharedPreferences keys exactly match Firestore fields:
    public static final String KEY_LAST_PLAYED_DATE  = UserFields.FIELD_LAST_PLAYED_DATE;    // "yyyy-MM-dd"
    public static final String KEY_LAST_PLAYED_TS    = UserFields.FIELD_LAST_PLAYED_TS;      // raw millis
    private static final String KEY_CURRENT_STREAK   = UserFields.FIELD_CURRENT_STREAK;
    private static final String KEY_TOTAL_XP         = UserFields.FIELD_TOTAL_XP;

    private final SharedPreferences prefs;
    private final FirebaseService firebaseService;

    public UserRepository(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.firebaseService = FirebaseService.getInstance();
    }

    /**
     * Sync remote Firestore → local SharedPreferences for:
     *   - lastPlayedDate (yyyy-MM-dd)
     *   - lastPlayedTimestamp (millis)
     *   - currentStreak (int)
     *   - totalXP (int)
     *
     * Call once (e.g. on sign-in or MainActivity onCreate) to merge remote values down to local.
     * If not signed in, returns null.
     */
    public Task<DocumentSnapshot> syncUserData() {
        if (!firebaseService.isUserSignedIn()) {
            return null;
        }

        return firebaseService.getUserDocument().get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    SharedPreferences.Editor editor = prefs.edit();

                    if (snapshot.contains(KEY_CURRENT_STREAK)) {
                        editor.putInt(
                                KEY_CURRENT_STREAK,
                                snapshot.getLong(KEY_CURRENT_STREAK).intValue()
                        );
                    }
                    if (snapshot.contains(KEY_TOTAL_XP)) {
                        editor.putInt(
                                KEY_TOTAL_XP,
                                snapshot.getLong(KEY_TOTAL_XP).intValue()
                        );
                    }
                    if (snapshot.contains(KEY_LAST_PLAYED_DATE)) {
                        editor.putString(
                                KEY_LAST_PLAYED_DATE,
                                snapshot.getString(KEY_LAST_PLAYED_DATE)
                        );
                    }
                    if (snapshot.contains(KEY_LAST_PLAYED_TS)) {
                        editor.putLong(
                                KEY_LAST_PLAYED_TS,
                                snapshot.getLong(KEY_LAST_PLAYED_TS)
                        );
                    }
                    editor.apply();
                });
    }

    /**
     * Whenever the user finishes a game, call this to update:
     *   - lastPlayedDate  = today (yyyy-MM-dd)
     *   - lastPlayedTimestamp = System.currentTimeMillis()
     *   - currentStreak, totalXP, last<GameType>Score  (and any trophies)
     *
     * If signed in, fetch remote first to keep cross-device streak continuity.
     * Otherwise, update local only.
     */
    public Task<Void> updateGameResults(String gameType, int score, int xpEarned) {
        // 1) Compute today / yesterday strings
        Calendar now = Calendar.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now.getTime());

        now.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now.getTime());

        // Timestamp for “today”:
        long nowMillis = System.currentTimeMillis();

        // If signed in, fetch remote first
        if (firebaseService.isUserSignedIn()) {
            return firebaseService.getUserDocument().get().continueWithTask(fetchTask -> {
                // Fallback to local only if fetch fails
                if (!fetchTask.isSuccessful() || fetchTask.getResult() == null) {
                    updateLocalOnly(gameType, score, xpEarned, today, yesterday, nowMillis);
                    return Tasks.forResult(null);
                }

                DocumentSnapshot snapshot = fetchTask.getResult();
                String remoteLastDate = "";
                long remoteLastTs = 0;
                int remoteStreak = 0;

                if (snapshot.exists()) {
                    if (snapshot.contains(KEY_LAST_PLAYED_DATE)) {
                        remoteLastDate = snapshot.getString(KEY_LAST_PLAYED_DATE);
                    }
                    if (snapshot.contains(KEY_LAST_PLAYED_TS)) {
                        remoteLastTs = snapshot.getLong(KEY_LAST_PLAYED_TS);
                    }
                    if (snapshot.contains(KEY_CURRENT_STREAK)) {
                        remoteStreak = snapshot.getLong(KEY_CURRENT_STREAK).intValue();
                    }
                }

                // 2) Compute updatedStreak:
                int updatedStreak;
                if (yesterday.equals(remoteLastDate)) {
                    // consecutive day
                    updatedStreak = remoteStreak + 1;
                } else if (!today.equals(remoteLastDate)) {
                    // missed ≥1 day or first-ever
                    updatedStreak = 1;
                } else {
                    // already played today (same date)
                    updatedStreak = remoteStreak;
                }

                // 3) Update local SharedPreferences now that we know updatedStreak:
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_LAST_PLAYED_DATE, today);
                editor.putLong(KEY_LAST_PLAYED_TS, nowMillis);
                editor.putInt(KEY_CURRENT_STREAK, updatedStreak);
                editor.putInt(KEY_TOTAL_XP, prefs.getInt(KEY_TOTAL_XP, 0) + xpEarned);
                editor.apply();

                // 4) Build Firestore update map
                Map<String, Object> updates = new HashMap<>();
                updates.put(KEY_LAST_PLAYED_DATE, today);
                updates.put(KEY_LAST_PLAYED_TS, nowMillis);
                updates.put(KEY_CURRENT_STREAK, updatedStreak);
                updates.put(KEY_TOTAL_XP, FieldValue.increment(xpEarned));
                updates.put(UserFields.lastGameScoreField(gameType), score);

                if (updatedStreak == 7) {
                    updates.put(UserFields.FIELD_TROPHIES, FieldValue.arrayUnion("7DayStreak"));
                }

                // 5) Merge into Firestore, then log a session entry
                return firebaseService.getUserDocument()
                        .set(updates, SetOptions.merge())
                        .continueWithTask(setTask ->
                                firebaseService.logGameSession(gameType, score, xpEarned)
                        );
            });
        }

        // If not signed in, purely local update
        updateLocalOnly(gameType, score, xpEarned, today, yesterday, nowMillis);
        // Reschedule purely from SharedPrefs:
        StreakNotificationScheduler.scheduleFromSharedPrefs(
                /*uid=*/ null,
                /*any context=*/ firebaseService.getAuth().getApp().getApplicationContext()
        );
        return null;
    }

    /** Local‐only fallback if offline or not signed in. */
    private void updateLocalOnly(
            String gameType,
            int score,
            int xpEarned,
            String today,
            String yesterday,
            long nowMillis) {

        String lastPlayedDate = prefs.getString(KEY_LAST_PLAYED_DATE, "");
        int currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0);

        int updatedStreak;
        if (yesterday.equals(lastPlayedDate)) {
            updatedStreak = currentStreak + 1;
        } else if (!today.equals(lastPlayedDate)) {
            updatedStreak = 1;
        } else {
            updatedStreak = currentStreak; // already played today
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LAST_PLAYED_DATE, today);
        editor.putLong(KEY_LAST_PLAYED_TS, nowMillis);
        editor.putInt(KEY_CURRENT_STREAK, updatedStreak);
        editor.putInt(KEY_TOTAL_XP, prefs.getInt(KEY_TOTAL_XP, 0) + xpEarned);
        editor.apply();

        // No Firestore when not signed in
    }

    /** Returns locally stored currentStreak. */
    public int getCurrentStreak() {
        return prefs.getInt(KEY_CURRENT_STREAK, 0);
    }

    /** Returns locally stored totalXP. */
    public int getTotalXP() {
        return prefs.getInt(KEY_TOTAL_XP, 0);
    }

    /** Returns locally stored lastPlayedDate ("yyyy-MM-dd"). */
    public String getLastPlayedDate() {
        return prefs.getString(KEY_LAST_PLAYED_DATE, "");
    }

    /** Returns locally stored lastPlayedTimestamp (millis). */
    public long getLastPlayedTimestamp() {
        return prefs.getLong(KEY_LAST_PLAYED_TS, -1L);
    }
}
