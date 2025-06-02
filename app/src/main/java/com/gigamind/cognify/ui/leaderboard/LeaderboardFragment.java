package com.gigamind.cognify.ui.leaderboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gigamind.cognify.R;
import com.gigamind.cognify.databinding.FragmentLeaderboardBinding;
import com.gigamind.cognify.ui.OnboardingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {
    private FragmentLeaderboardBinding binding;
    private LeaderboardAdapter adapter;
    private FirebaseFirestore db;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        setupSwipeAndButtons();
        loadLeaderboard();
    }

    private void setupViews() {
        binding.swipeRefresh.setOnRefreshListener(this::loadLeaderboard);
        
        // Setup sign in button if needed
        if (binding.signInPrompt.getVisibility() == View.VISIBLE) {
            binding.signInButton.setOnClickListener(v -> {
                // Handle sign in - you should implement this based on your auth flow
                Toast.makeText(requireContext(), "Sign in to view leaderboard", 
                             Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupSwipeAndButtons() {
        binding.swipeRefresh.setOnRefreshListener(this::loadLeaderboard);

        // If “Retry” is pressed in error state:
        binding.retryButton.setOnClickListener(v -> loadLeaderboard());

        // If “Sign In” pressed in signInPrompt:
        binding.signInButton.setOnClickListener(v -> {
            // Launch your existing Google‐SignIn flow or OnboardingActivity
            // so that user can log in. Adjust to your code.
            startActivity(new Intent(requireContext(), OnboardingActivity.class));
        });
    }

    private void loadLeaderboard() {
        if (isLoading) return;
        isLoading = true;

        // Show SwipeLoading
        binding.swipeRefresh.setRefreshing(true);
        binding.leaderboard.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.GONE);
        binding.errorView.setVisibility(View.GONE);
        binding.signInPrompt.setVisibility(View.GONE);

        // Check if user is signed in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Not signed in
            binding.signInPrompt.setVisibility(View.VISIBLE);
            binding.swipeRefresh.setRefreshing(false);
            isLoading = false;
            return;
        }

        // Query the top 100 by totalXP descending
        db.collection("users")
                .orderBy("totalXP", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LeaderboardItem> items = new ArrayList<>();
                    int rank = 1;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        LeaderboardItem item = doc.toObject(LeaderboardItem.class);
                        item.setUserId(doc.getId());                         // store UID
                        item.setRank(rank++);                                // assign rank
                        // If the Firestore doc didn’t include “countryCode,” default to empty:
                        if (item.getCountryCode() == null) {
                            item.setCountryCode("");
                        }
                        items.add(item);
                    }

                    if (items.isEmpty()) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        binding.leaderboard.setVisibility(View.VISIBLE);
                        adapter.submitList(items);
                    }

                    binding.swipeRefresh.setRefreshing(false);
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    // Show error message
                    binding.errorView.setVisibility(View.VISIBLE);
                    binding.errorText.setText(
                            getString(R.string.error_loading_leaderboard, e.getMessage())
                    );
                    binding.swipeRefresh.setRefreshing(false);
                    isLoading = false;
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 