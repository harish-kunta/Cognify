package com.gigamind.cognify.data.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Map;
import java.util.HashMap;

public class FirebaseService {
    private static FirebaseService instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    // Collection paths
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_SESSIONS = "sessions";

    private FirebaseService() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isUserSignedIn() {
        return getCurrentUser() != null;
    }

    public DocumentReference getUserDocument() {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No user signed in");
        }
        return firestore.collection(COLLECTION_USERS).document(user.getUid());
    }

    public Task<Void> updateUserData(Map<String, Object> updates) {
        return getUserDocument().set(updates, SetOptions.merge());
    }

    public Task<Void> updateUserScore(String gameType, int score, int xpEarned) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastGameScore", score);
        updates.put("totalXP", xpEarned);
        
        if (gameType != null) {
            updates.put("last" + gameType + "Score", score);
        }
        
        return updateUserData(updates);
    }

    public Task<Void> logGameSession(String gameType, int score, int xpEarned) {
        FirebaseUser user = getCurrentUser();
        if (user == null) return null;

        return firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .collection(COLLECTION_SESSIONS)
                .document(gameType.toLowerCase())
                .collection("entries")
                .add(new HashMap<String, Object>() {{
                    put("score", score);
                    put("xpGained", xpEarned);
                    put("timestamp", com.google.firebase.Timestamp.now());
                }}).continueWith(task -> null);
    }

    public void signOut() {
        auth.signOut();
    }

    /**
     * Deletes the current Firebase user account and associated Firestore data.
     * Caller should ensure the user has recently reauthenticated.
     */
    public Task<Void> deleteAccountAndData() {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            return Tasks.forException(new Exception("No user signed in"));
        }

        String uid = user.getUid();
        Task<Void> deleteData = firestore.collection(COLLECTION_USERS)
                .document(uid)
                .delete();

        return deleteData.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                return Tasks.forException(task.getException());
            }
            return user.delete();
        }).addOnCompleteListener(t -> signOut());
    }
}
