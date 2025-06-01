package com.gigamind.cognify.ui.leaderboard;

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
import com.google.firebase.auth.FirebaseAuth;
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
        setupViews();
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

    private void loadLeaderboard() {
        if (isLoading) return;
        isLoading = true;
        binding.swipeRefresh.setRefreshing(true);
        binding.errorView.setVisibility(View.GONE);

        // Check if user is signed in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            showSignInPrompt();
            isLoading = false;
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        db.collection("scores")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LeaderboardItem> items = new ArrayList<>();
                    int rank = 1;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        LeaderboardItem item = document.toObject(LeaderboardItem.class);
                        item.setRank(rank++);
                        items.add(item);
                    }
                    
                    if (items.isEmpty()) {
                        showEmptyState();
                    } else {
                        showLeaderboard(items);
                    }
                    
                    isLoading = false;
                    binding.swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    showError(e.getMessage());
                    isLoading = false;
                    binding.swipeRefresh.setRefreshing(false);
                });
    }

    private void showSignInPrompt() {
        binding.signInPrompt.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.errorView.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.GONE);
    }

    private void showLeaderboard(List<LeaderboardItem> items) {
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.signInPrompt.setVisibility(View.GONE);
        binding.errorView.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.GONE);
        adapter.submitList(items);
    }

    private void showEmptyState() {
        binding.emptyView.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.signInPrompt.setVisibility(View.GONE);
        binding.errorView.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.errorView.setVisibility(View.VISIBLE);
        binding.errorText.setText(getString(R.string.error_loading_leaderboard, message));
        binding.recyclerView.setVisibility(View.GONE);
        binding.signInPrompt.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 