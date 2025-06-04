package com.gigamind.cognify.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.util.UserFields;
import com.gigamind.cognify.work.StreakNotificationScheduler;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * UserRepository now keeps a real-time Firestore listener on the user document,
 * so that “currentStreak” and “totalXP” (etc.) load from local cache immediately
 * and only update once the server response arrives—eliminating any flicker.
 */
public class UserRepository {
    private static final String PREFS_NAME = "GamePrefs";

    // SharedPreferences keys, matching Firestore fields:
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
     * “Real-time” listener that keeps SharedPreferences in sync with Firestore.
     * Returns a ListenerRegistration so the caller can remove() it when no longer needed.
     *
     * As soon as this is attached, Firestore will fire the listener with any cached document,
     * and immediately thereafter with the server copy. That way there’s no flicker from a stale default.
     *
     * If the user is not signed in, this returns null.
     */
    @Nullable
    public ListenerRegistration attachUserDocumentListener(final OnUserDataChanged callback) {
        if (!firebaseService.isUserSignedIn()) {
            return null;
        }

        DocumentReference docRef = firebaseService.getUserDocument();
        return docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // Could log network / permission error, but don’t crash.
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    SharedPreferences.Editor editor = prefs.edit();

                    // Update “currentStreak”
                    if (snapshot.contains(KEY_CURRENT_STREAK)) {
                        int streak = snapshot.getLong(KEY_CURRENT_STREAK).intValue();
                        editor.putInt(KEY_CURRENT_STREAK, streak);
                    }
                    // Update “totalXP”
                    if (snapshot.contains(KEY_TOTAL_XP)) {
                        int xp = snapshot.getLong(KEY_TOTAL_XP).intValue();
                        editor.putInt(KEY_TOTAL_XP, xp);
                    }
                    // Update “lastPlayedDate”
                    if (snapshot.contains(KEY_LAST_PLAYED_DATE)) {
                        String dateStr = snapshot.getString(KEY_LAST_PLAYED_DATE);
                        editor.putString(KEY_LAST_PLAYED_DATE, dateStr);
                    }
                    // Update “lastPlayedTimestamp”
                    if (snapshot.contains(KEY_LAST_PLAYED_TS)) {
                        long ts = snapshot.getLong(KEY_LAST_PLAYED_TS);
                        editor.putLong(KEY_LAST_PLAYED_TS, ts);
                    }
                    editor.apply();

                    // Notify the UI (on the main thread) to read from SharedPreferences
                    callback.onDataChanged();
                }
            }
        });
    }

    /**
     * If you still need a “one-time sync” (e.g. on sign-in), you can call this:
     * but in most “live” screens you’ll prefer attachUserDocumentListener().
     */
    @Nullable
    public Task<DocumentSnapshot> syncUserDataOnce() {
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
     *   - currentStreak, totalXP, last<GameType>Score (and any trophies)
     *
     * If signed in, fetch remote first to preserve cross-device continuity.
     * Otherwise, fall back to purely local update.
     */
    public Task<Void> updateGameResults(String gameType, int score, int xpEarned) {
        // 1) Compute “today” and “yesterday”
        Calendar now = Calendar.getInstance();
        String today     = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now.getTime());
        now.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now.getTime());

        // 2) Current timestamp
        long nowMillis = System.currentTimeMillis();

        if (firebaseService.isUserSignedIn()) {
            return firebaseService.getUserDocument().get()
                    .continueWithTask(fetchTask -> {
                        // If fetch failed, do local only
                        if (!fetchTask.isSuccessful() || fetchTask.getResult() == null) {
                            updateLocalOnly(gameType, score, xpEarned, today, yesterday, nowMillis);
                            return Tasks.forResult(null);
                        }

                        DocumentSnapshot snapshot = fetchTask.getResult();
                        String remoteLastDate = "";
                        long remoteLastTs    = 0;
                        int remoteStreak     = 0;

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

                        // 3) Compute updatedStreak
                        final int updatedStreak;
                        if (yesterday.equals(remoteLastDate)) {
                            // continuing streak
                            updatedStreak = remoteStreak + 1;
                        } else if (!today.equals(remoteLastDate)) {
                            // missed at least one day (or first ever)
                            updatedStreak = 1;
                        } else {
                            // already played today
                            updatedStreak = remoteStreak;
                        }

                        // 4) Write into SharedPreferences immediately
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(KEY_LAST_PLAYED_DATE, today);
                        editor.putLong(KEY_LAST_PLAYED_TS, nowMillis);
                        editor.putInt(KEY_CURRENT_STREAK, updatedStreak);
                        editor.putInt(KEY_TOTAL_XP, prefs.getInt(KEY_TOTAL_XP, 0) + xpEarned);
                        editor.apply();

                        // 5) Build Firestore update map
                        Map<String, Object> updates = new HashMap<>();
                        updates.put(KEY_LAST_PLAYED_DATE, today);
                        updates.put(KEY_LAST_PLAYED_TS, nowMillis);
                        updates.put(KEY_CURRENT_STREAK, updatedStreak);
                        updates.put(KEY_TOTAL_XP, FieldValue.increment(xpEarned));
                        updates.put(UserFields.lastGameScoreField(gameType), score);

                        if (updatedStreak == 7) {
                            updates.put(UserFields.FIELD_TROPHIES, FieldValue.arrayUnion("7DayStreak"));
                        }

                        return firebaseService.getUserDocument()
                                .set(updates, SetOptions.merge())
                                .continueWithTask(setTask ->
                                        firebaseService.logGameSession(gameType, score, xpEarned)
                                );
                    });
        }

        // 6) Not signed in (or offline) → purely local
        updateLocalOnly(gameType, score, xpEarned, today, yesterday, nowMillis);
        // Reschedule notification from the (now-updated) SharedPreferences:
        StreakNotificationScheduler.scheduleFromSharedPrefs(
                /*uid=*/ null,
                /*appCtx=*/ null  // Your scheduler can retrieve the prefs itself
        );
        return null;
    }

    /** Local-only fallback if the user isn’t signed in or fetch failed. */
    private void updateLocalOnly(
            String gameType,
            int score,
            int xpEarned,
            String today,
            String yesterday,
            long nowMillis) {

        String lastPlayedDate = prefs.getString(KEY_LAST_PLAYED_DATE, "");
        int   currentStreak   = prefs.getInt(KEY_CURRENT_STREAK, 0);

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
    }

    /** Returns locally stored “currentStreak.” */
    public int getCurrentStreak() {
        return prefs.getInt(KEY_CURRENT_STREAK, 0);
    }

    /** Returns locally stored “totalXP.” */
    public int getTotalXP() {
        return prefs.getInt(KEY_TOTAL_XP, 0);
    }

    /** Returns locally stored “lastPlayedDate” (yyyy-MM-dd). */
    public String getLastPlayedDate() {
        return prefs.getString(KEY_LAST_PLAYED_DATE, "");
    }

    /** Returns locally stored “lastPlayedTimestamp” (millis). */
    public long getLastPlayedTimestamp() {
        return prefs.getLong(KEY_LAST_PLAYED_TS, -1L);
    }

    /** Callback interface: UI should implement this to refresh its views. */
    public interface OnUserDataChanged {
        void onDataChanged();
    }
}
