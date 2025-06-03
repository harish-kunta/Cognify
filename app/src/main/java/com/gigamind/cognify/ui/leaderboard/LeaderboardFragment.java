package com.gigamind.cognify.ui.leaderboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gigamind.cognify.R;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.databinding.FragmentLeaderboardBinding;
import com.gigamind.cognify.ui.OnboardingActivity;
import com.gigamind.cognify.util.UserFields;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {
    private FragmentLeaderboardBinding binding;
    private LeaderboardAdapter adapter;
    private FirebaseService firebaseService;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseService = FirebaseService.getInstance();
        setupRecyclerView();
        setupSwipeAndButtons();
        loadLeaderboard();
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupSwipeAndButtons() {
        binding.swipeRefresh.setOnRefreshListener(this::loadLeaderboard);
        binding.retryButton.setOnClickListener(v -> loadLeaderboard());
        binding.signInButton.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), OnboardingActivity.class));
        });
    }

    private void showView(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
            view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
        }
    }

    private void hideView(View view) {
        if (view.getVisibility() != View.GONE) {
            view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out));
            view.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> loadLeaderboard())
                .show();
    }

    private void loadLeaderboard() {
        if (isLoading) return;
        isLoading = true;

        // Show loading state
        binding.swipeRefresh.setRefreshing(true);
        hideView(binding.leaderboard);
        hideView(binding.emptyView);
        hideView(binding.errorView);
        hideView(binding.signInPrompt);

        // Check if user is signed in
        if (!firebaseService.isUserSignedIn()) {
            binding.swipeRefresh.setRefreshing(false);
            showView(binding.signInPrompt);
            isLoading = false;
            return;
        }

        // Query top 100 users by totalXP (use constant)
        firebaseService.getFirestore()
                .collection(FirebaseService.COLLECTION_USERS)
                .orderBy(UserFields.FIELD_TOTAL_XP, Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LeaderboardItem> items = new ArrayList<>();
                    int rank = 1;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        LeaderboardItem item = doc.toObject(LeaderboardItem.class);
                        item.setUserId(doc.getId());
                        item.setRank(rank++);
                        if (item.getCountryCode() == null) {
                            item.setCountryCode("");
                        }
                        items.add(item);
                    }

                    binding.swipeRefresh.setRefreshing(false);

                    if (items.isEmpty()) {
                        showView(binding.emptyView);
                    } else {
                        showView(binding.leaderboard);
                        adapter.submitList(items);
                    }
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    binding.swipeRefresh.setRefreshing(false);
                    showView(binding.errorView);
                    binding.errorText.setText(
                            getString(R.string.error_loading_leaderboard, e.getMessage())
                    );
                    showError(getString(R.string.error_loading_leaderboard, e.getMessage()));
                    isLoading = false;
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
