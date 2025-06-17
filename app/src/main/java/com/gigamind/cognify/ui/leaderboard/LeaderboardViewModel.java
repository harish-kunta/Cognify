// file: com/gigamind/cognify/ui/leaderboard/LeaderboardViewModel.java
package com.gigamind.cognify.ui.leaderboard;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.util.UserFields;
import com.gigamind.cognify.util.ExceptionLogger;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a cached copy of the top‐100 leaderboard.  Only fetches from Firestore once
 * (unless refresh() is called).  Exposes LiveData so that fragments can simply observe
 * and display whatever is in memory, avoiding flicker when re‐attached.
 */
public class LeaderboardViewModel extends ViewModel {
    private final FirebaseService firebaseService = FirebaseService.getInstance();

    // “_leaderboard” holds our in‐memory list; we expose it as LiveData:
    private final MutableLiveData<List<LeaderboardItem>> _leaderboard = new MutableLiveData<>();
    public LiveData<List<LeaderboardItem>> leaderboard = _leaderboard;

    // A flag so we only kick off one Firestore query at a time:
    private boolean isFetching = false;

    /**
     * Kick off a one‐time Firestore query for top‐100 users by totalXP.  If a query is already
     * in progress, this does nothing.  Once the results arrive, they are posted into _leaderboard.
     *
     * If you want to force a re‐download (e.g. pull‐to‐refresh), call refresh() instead.
     */
    public void loadOnce() {
        if (isFetching || _leaderboard.getValue() != null) {
            // Either already loading, or we've already loaded data before—do nothing.
            return;
        }
        isFetching = true;

        firebaseService.getFirestore()
                .collection(FirebaseService.COLLECTION_USERS)
                .orderBy(UserFields.FIELD_TOTAL_XP, Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<LeaderboardItem> items = new ArrayList<>();
                    int rank = 1;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        LeaderboardItem item = doc.toObject(LeaderboardItem.class);
                        item.setUserId(doc.getId());
                        item.setRank(rank++);
                        if (item.getCountryCode() == null) {
                            item.setCountryCode("");
                        }
                        items.add(item);
                    }
                    _leaderboard.postValue(items);
                    isFetching = false;
                })
                .addOnFailureListener(e -> {
                    ExceptionLogger.log("LeaderboardViewModel", e);
                    // You could post an empty list or a special “error” sentinel.  For simplicity,
                    // we'll post null to indicate failure; the fragment can check for null.
                    _leaderboard.postValue(null);
                    isFetching = false;
                });
    }

    /**
     * Forces a brand‐new fetch from Firestore, even if data was already in memory.
     * Does exactly the same query; overwrites the old list.
     */
    public void refresh() {
        // Reset our in‐memory list to “no data yet,” so observers know to re‐show a spinner.
        _leaderboard.setValue(null);
        isFetching = false;
        loadOnce();
    }
}
