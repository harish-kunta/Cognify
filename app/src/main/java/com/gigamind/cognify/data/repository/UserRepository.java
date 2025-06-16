package com.gigamind.cognify.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.util.UserFields;
import com.gigamind.cognify.util.Constants;
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
import com.gigamind.cognify.util.DateUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * UserRepository now keeps a real-time Firestore listener on the user document,
 * so that “currentStreak” and “totalXP” (etc.) load from local cache immediately
 * and only update once the server response arrives—eliminating any flicker.
 */
public class UserRepository {
    // SharedPreferences keys, matching Firestore fields:
    public static final String KEY_LAST_PLAYED_DATE = UserFields.FIELD_LAST_PLAYED_DATE;    // "yyyy-MM-dd"
    public static final String KEY_LAST_PLAYED_TS = UserFields.FIELD_LAST_PLAYED_TS;      // raw millis
    public static final String KEY_CURRENT_STREAK = UserFields.FIELD_CURRENT_STREAK;
    public static final String KEY_TOTAL_XP = UserFields.FIELD_TOTAL_XP;
    public static final String KEY_PERSONAL_BEST_XP = UserFields.FIELD_PERSONAL_BEST_XP;
    // Additional local-only stats
    public static final String KEY_TOTAL_GAMES = "totalGames";
    public static final String KEY_WINS = "totalWins";
    public static final String KEY_LOSSES = "totalLosses";
    private static final String PREFS_NAME = Constants.PREFS_NAME;
    private final SharedPreferences prefs;
    private final FirebaseService firebaseService;
    private Context context;

    public UserRepository(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.firebaseService = FirebaseService.getInstance();
    }

    @Nullable
    public Task<DocumentSnapshot> syncUserDataOnce() {
        if (!firebaseService.isUserSignedIn()) {
            return null;
        }

        return firebaseService.getUserDocument().get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    SharedPreferences.Editor editor = prefs.edit();

                    if (snapshot.contains(UserFields.FIELD_CURRENT_STREAK)) {
                        int streak = snapshot.getLong(UserFields.FIELD_CURRENT_STREAK).intValue();
                        editor.putInt(KEY_CURRENT_STREAK, streak);
                    }
                    if (snapshot.contains(UserFields.FIELD_TOTAL_XP)) {
                        int xp = snapshot.getLong(UserFields.FIELD_TOTAL_XP).intValue();
                        editor.putInt(KEY_TOTAL_XP, xp);
                    }
                    if (snapshot.contains(UserFields.FIELD_LAST_PLAYED_DATE)) {
                        String dateStr = snapshot.getString(UserFields.FIELD_LAST_PLAYED_DATE);
                        editor.putString(KEY_LAST_PLAYED_DATE, dateStr);
                    }
                    if (snapshot.contains(UserFields.FIELD_LAST_PLAYED_TS)) {
                        long ts = snapshot.getLong(UserFields.FIELD_LAST_PLAYED_TS);
                        editor.putLong(KEY_LAST_PLAYED_TS, ts);
                    }

                    for (String type : new String[]{Constants.GAME_TYPE_WORD_DASH, Constants.TYPE_QUICK_MATH}) {
                        String xpField = UserFields.totalGameXpField(type);
                        if (snapshot.contains(xpField)) {
                            int xpVal = snapshot.getLong(xpField).intValue();
                            editor.putInt(xpField, xpVal);
                        }
                        String scoreField = UserFields.lastGameScoreField(type);
                        if (snapshot.contains(scoreField)) {
                            int sVal = snapshot.getLong(scoreField).intValue();
                            editor.putInt(scoreField, sVal);
                        }
                    }
                    editor.apply();
                });
    }

    /**
     * “Real-time” listener that keeps SharedPreferences in sync with Firestore.
     * Returns a ListenerRegistration so the caller can remove() it when no longer needed.
     * <p>
     * As soon as this is attached, Firestore will fire the listener with any cached document,
     * and immediately thereafter with the server copy. That way there’s no flicker from a stale default.
     * <p>
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

                    for (String type : new String[]{Constants.GAME_TYPE_WORD_DASH, Constants.TYPE_QUICK_MATH}) {
                        String xpField = UserFields.totalGameXpField(type);
                        if (snapshot.contains(xpField)) {
                            int xpVal = snapshot.getLong(xpField).intValue();
                            editor.putInt(xpField, xpVal);
                        }
                        String scoreField = UserFields.lastGameScoreField(type);
                        if (snapshot.contains(scoreField)) {
                            int sVal = snapshot.getLong(scoreField).intValue();
                            editor.putInt(scoreField, sVal);
                        }
                    }

                    if (snapshot.contains(KEY_PERSONAL_BEST_XP)) {
                        // Write into SharedPrefs under “pb_<gameType>”
                        int pbVal = snapshot.getLong(KEY_PERSONAL_BEST_XP).intValue();
                        editor.putInt(KEY_PERSONAL_BEST_XP, pbVal);
                    }
                    editor.apply();

                    // Notify the UI (on the main thread) to read from SharedPreferences
                    callback.onDataChanged();
                }
            }
        });
    }

    /**
     * Creates a new user document in Firestore if it doesn't exist, otherwise
     * updates the existing profile fields. SharedPreferences are updated with
     * default values on first creation. Returns a Task that completes once the
     * operation (and any subsequent sync) is finished.
     */
    public Task<Void> createOrUpdateUser(String uid, String name, String email) {
        DocumentReference userRef = firebaseService.getFirestore()
                .collection(FirebaseService.COLLECTION_USERS)
                .document(uid);

        return userRef.get().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            DocumentSnapshot snapshot = task.getResult();
            if (snapshot != null && snapshot.exists()) {
                Map<String, Object> updates = new HashMap<>();
                updates.put(UserFields.FIELD_NAME, name != null ? name : "");
                updates.put(UserFields.FIELD_EMAIL, email != null ? email : "");
                return userRef.set(updates, SetOptions.merge())
                        .continueWithTask(t -> syncUserDataOnce());
            }

            Map<String, Object> newUserData = new HashMap<>();
            newUserData.put(UserFields.FIELD_UID, uid);
            newUserData.put(UserFields.FIELD_NAME, name != null ? name : "");
            newUserData.put(UserFields.FIELD_EMAIL, email != null ? email : "");
            newUserData.put(UserFields.FIELD_CURRENT_STREAK, 0);
            newUserData.put(UserFields.FIELD_TOTAL_XP, 0);
            newUserData.put(UserFields.FIELD_LAST_PLAYED_DATE, "");
            newUserData.put(UserFields.FIELD_LAST_PLAYED_TS, 0L);
            newUserData.put(UserFields.FIELD_LEADERBOARD_RANK, 0);
            newUserData.put(UserFields.FIELD_TROPHIES, new ArrayList<>());

            return userRef.set(newUserData, SetOptions.merge())
                    .continueWith(t -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(KEY_CURRENT_STREAK, 0);
                        editor.putInt(KEY_TOTAL_XP, 0);
                        editor.putString(KEY_LAST_PLAYED_DATE, "");
                        editor.putLong(KEY_LAST_PLAYED_TS, 0L);
                        editor.apply();
                        return null;
                    });
        });
    }

    /**
     * Whenever the user finishes a game, call this to update:
     * - lastPlayedDate  = today (yyyy-MM-dd)
     * - lastPlayedTimestamp = System.currentTimeMillis()
     * - currentStreak, totalXP, last<GameType>Score (and any trophies)
     * <p>
     * If signed in, fetch remote first to preserve cross-device continuity.
     * Otherwise, fall back to purely local update.
     */
    public Task<Void> updateGameResults(
            String gameType,
            int score,
            int xpEarned,
            boolean isWin,
            @Nullable Integer newPersonalBest  // if null, don’t write PB to Firestore
    ) {
        // 1) Compute “today” and “yesterday”
        String today = DateUtils.today();
        String yesterday = DateUtils.yesterday();

        // 2) Current timestamp
        long nowMillis = System.currentTimeMillis();

        if (firebaseService.isUserSignedIn()) {
            return firebaseService.getUserDocument().get()
                    .continueWithTask(fetchTask -> {
                        // If fetch failed, do local only
                        if (!fetchTask.isSuccessful() || fetchTask.getResult() == null) {
                            updateLocalOnly(gameType, score, xpEarned, isWin, today, yesterday, nowMillis);
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
                        editor.putInt(UserFields.totalGameXpField(gameType),
                                prefs.getInt(UserFields.totalGameXpField(gameType), 0) + xpEarned);
                        editor.putInt(UserFields.lastGameScoreField(gameType), score);
                        editor.putInt(KEY_TOTAL_GAMES, prefs.getInt(KEY_TOTAL_GAMES, 0) + 1);
                        if (isWin) {
                            editor.putInt(KEY_WINS, prefs.getInt(KEY_WINS, 0) + 1);
                        } else {
                            editor.putInt(KEY_LOSSES, prefs.getInt(KEY_LOSSES, 0) + 1);
                        }
                        editor.apply();

                        // 5) Build Firestore update map
                        Map<String, Object> updates = new HashMap<>();
                        updates.put(KEY_LAST_PLAYED_DATE, today);
                        updates.put(KEY_LAST_PLAYED_TS, nowMillis);
                        updates.put(KEY_CURRENT_STREAK, updatedStreak);
                        updates.put(KEY_TOTAL_XP, FieldValue.increment(xpEarned));
                        updates.put(UserFields.totalGameXpField(gameType), FieldValue.increment(xpEarned));
                        updates.put(UserFields.lastGameScoreField(gameType), score);

                        if (newPersonalBest != null) {
                            updates.put(KEY_PERSONAL_BEST_XP, newPersonalBest);
                        }

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
        updateLocalOnly(gameType, score, xpEarned, isWin, today, yesterday, nowMillis);
        // Reschedule notification from the (now-updated) SharedPreferences:
        StreakNotificationScheduler.scheduleFromSharedPrefs(
                null, context
        );
        return null;
    }

    /**
     * Local-only fallback if the user isn’t signed in or fetch failed.
     */
    private void updateLocalOnly(
            String gameType,
            int score,
            int xpEarned,
            boolean isWin,
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
        editor.putInt(UserFields.totalGameXpField(gameType),
                prefs.getInt(UserFields.totalGameXpField(gameType), 0) + xpEarned);
        editor.putInt(UserFields.lastGameScoreField(gameType), score);
        editor.putInt(KEY_TOTAL_GAMES, prefs.getInt(KEY_TOTAL_GAMES, 0) + 1);
        if (isWin) {
            editor.putInt(KEY_WINS, prefs.getInt(KEY_WINS, 0) + 1);
        } else {
            editor.putInt(KEY_LOSSES, prefs.getInt(KEY_LOSSES, 0) + 1);
        }
        editor.apply();
    }

    /**
     * Returns locally stored “currentStreak.”
     */
    public int getCurrentStreak() {
        return prefs.getInt(KEY_CURRENT_STREAK, 0);
    }

    /**
     * Returns locally stored “totalXP.”
     */
    public int getTotalXP() {
        return prefs.getInt(KEY_TOTAL_XP, 0);
    }

    /**
     * Returns locally stored total XP for the specified game type.
     */
    public int getTotalGameXp(String gameType) {
        return prefs.getInt(UserFields.totalGameXpField(gameType), 0);
    }

    public int getTotalGames() {
        return prefs.getInt(KEY_TOTAL_GAMES, 0);
    }

    public int getTotalWins() {
        return prefs.getInt(KEY_WINS, 0);
    }

    public int getTotalLosses() {
        return prefs.getInt(KEY_LOSSES, 0);
    }

    /**
     * Returns locally stored “lastPlayedDate” (yyyy-MM-dd).
     */
    public String getLastPlayedDate() {
        return prefs.getString(KEY_LAST_PLAYED_DATE, "");
    }

    /**
     * Returns locally stored “lastPlayedTimestamp” (millis).
     */
    public long getLastPlayedTimestamp() {
        return prefs.getLong(KEY_LAST_PLAYED_TS, -1L);
    }

    /**
     * Returns the locally stored score for the given game type.
     */
    public int getLastGameScore(String gameType) {
        return prefs.getInt(UserFields.lastGameScoreField(gameType), 0);
    }

    /**
     * Callback interface: UI should implement this to refresh its views.
     */
    public interface OnUserDataChanged {
        void onDataChanged();
    }
}
