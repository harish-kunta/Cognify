package com.gigamind.cognify.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.util.UserFields;
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
 * UserRepository now always checks Firestore first (if signed in) to compute streak,
 * so that a user’s streak is preserved across multiple devices.
 * All field names (Firestore keys + SharedPreferences keys) come from UserFields.
 */
public class UserRepository {
    private static final String PREFS_NAME = "GamePrefs";

    // SharedPreferences keys share the same names as Firestore fields
    public static final String KEY_LAST_PLAYED_DATE = UserFields.FIELD_LAST_PLAYED_DATE;
    private static final String KEY_CURRENT_STREAK   = UserFields.FIELD_CURRENT_STREAK;
    private static final String KEY_TOTAL_XP         = UserFields.FIELD_TOTAL_XP;

    private final SharedPreferences prefs;
    private final FirebaseService firebaseService;

    public UserRepository(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.firebaseService = FirebaseService.getInstance();
    }

    /**
     * Syncs remote Firestore → local SharedPreferences. Should be called once (e.g. at app launch)
     * if you want local UI to reflect remote data immediately. Returns the Task so you can chain
     * UI updates if needed.
     * If user not signed in, returns null.
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
                    if (snapshot.contains(KEY_LAST_PLAYED_DATE)) {
                        editor.putString(
                                KEY_LAST_PLAYED_DATE,
                                snapshot.getString(KEY_LAST_PLAYED_DATE)
                        );
                    }
                    if (snapshot.contains(KEY_TOTAL_XP)) {
                        editor.putInt(
                                KEY_TOTAL_XP,
                                snapshot.getLong(KEY_TOTAL_XP).intValue()
                        );
                    }
                    editor.apply();
                });
    }

    /**
     * Updates game results (score + XP) and recomputes streak. If the user is signed in,
     * this will first fetch remote fields from Firestore to preserve cross-device streak continuity.
     * Otherwise, falls back to local-only logic.
     *
     * @param gameType  the name of the game (used for "last<GameType>Score" field)
     * @param score     the score just achieved
     * @param xpEarned  the XP gained in this session
     * @return a Task<Void> that completes once Firestore has been merged and session logged.
     *         If user not signed in, returns null (because only local prefs are updated).
     */
    public Task<Void> updateGameResults(String gameType, int score, int xpEarned) {
        // 1) Compute “today” and “yesterday” strings in UTC-simple format “yyyy-MM-dd”
        Calendar now = Calendar.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(now.getTime());

        now.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(now.getTime());

        // If user is signed in, fetch remote to compute streak:
        if (firebaseService.isUserSignedIn()) {
            return firebaseService.getUserDocument()
                    .get()
                    .continueWithTask(fetchTask -> {
                        // If fetchTask failed, fall back to local-only logic:
                        if (!fetchTask.isSuccessful() || fetchTask.getResult() == null) {
                            updateLocalOnly(today, yesterday, xpEarned, gameType, score);
                            return Tasks.forResult(null);
                        }

                        DocumentSnapshot snapshot = fetchTask.getResult();
                        String remoteLastDate = "";
                        int remoteStreak = 0;

                        if (snapshot.exists()) {
                            if (snapshot.contains(KEY_LAST_PLAYED_DATE)) {
                                remoteLastDate = snapshot.getString(KEY_LAST_PLAYED_DATE);
                            }
                            if (snapshot.contains(KEY_CURRENT_STREAK)) {
                                remoteStreak = snapshot.getLong(KEY_CURRENT_STREAK).intValue();
                            }
                        }

                        // 2) Compute updatedStreak based on remote values
                        int updatedStreak;
                        if (yesterday.equals(remoteLastDate)) {
                            // Continuing streak
                            updatedStreak = remoteStreak + 1;
                        } else if (!today.equals(remoteLastDate)) {
                            // Missed at least one day (or first play ever)
                            updatedStreak = 1;
                        } else {
                            // Already played today
                            updatedStreak = remoteStreak;
                        }

                        // 3) Update local SharedPreferences now that we know updatedStreak
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(KEY_LAST_PLAYED_DATE, today);
                        editor.putInt(KEY_CURRENT_STREAK, updatedStreak);
                        editor.putInt(KEY_TOTAL_XP, prefs.getInt(KEY_TOTAL_XP, 0) + xpEarned);
                        editor.apply();

                        // 4) Build Firestore update map
                        Map<String, Object> updates = new HashMap<>();
                        updates.put(KEY_LAST_PLAYED_DATE, today);
                        updates.put(KEY_CURRENT_STREAK, updatedStreak);
                        // Atomic increment of totalXP
                        updates.put(KEY_TOTAL_XP, FieldValue.increment(xpEarned));
                        // last<GameType>Score field:
                        updates.put(UserFields.lastGameScoreField(gameType), score);

                        // Award trophy if streak hits 7
                        if (updatedStreak == 7) {
                            updates.put(UserFields.FIELD_TROPHIES, FieldValue.arrayUnion("7DayStreak"));
                        }

                        // 5) Merge into Firestore, then log session
                        return firebaseService.getUserDocument()
                                .set(updates, SetOptions.merge())
                                .continueWithTask(setTask ->
                                        firebaseService.logGameSession(gameType, score, xpEarned)
                                );
                    });
        }

        // If not signed in, do local-only update
        updateLocalOnly(today, yesterday, xpEarned, gameType, score);
        return null;
    }

    /**
     * Fallback local-only logic if the user isn’t signed in (or Firestore fetch failed).
     * Computes streak from SharedPreferences and updates everything locally.
     */
    private void updateLocalOnly(String today,
                                 String yesterday,
                                 int xpEarned,
                                 String gameType,
                                 int score) {
        String lastPlayedDate = prefs.getString(KEY_LAST_PLAYED_DATE, "");
        int currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0);

        int updatedStreak;
        if (yesterday.equals(lastPlayedDate)) {
            updatedStreak = currentStreak + 1;
        } else if (!today.equals(lastPlayedDate)) {
            updatedStreak = 1;
        } else {
            updatedStreak = currentStreak;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LAST_PLAYED_DATE, today);
        editor.putInt(KEY_CURRENT_STREAK, updatedStreak);
        editor.putInt(KEY_TOTAL_XP, prefs.getInt(KEY_TOTAL_XP, 0) + xpEarned);
        editor.apply();

        // (No Firestore logging when not signed in.)
    }

    /** Returns the locally stored “currentStreak.” */
    public int getCurrentStreak() {
        return prefs.getInt(KEY_CURRENT_STREAK, 0);
    }

    /** Returns the locally stored “totalXP.” */
    public int getTotalXP() {
        return prefs.getInt(KEY_TOTAL_XP, 0);
    }

    /** Returns the locally stored “lastPlayedDate.” */
    public String getLastPlayedDate() {
        return prefs.getString(KEY_LAST_PLAYED_DATE, "");
    }
}
