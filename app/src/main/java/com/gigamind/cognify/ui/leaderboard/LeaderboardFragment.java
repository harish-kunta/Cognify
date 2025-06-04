// file: com/gigamind/cognify/ui/leaderboard/LeaderboardFragment.java
package com.gigamind.cognify.ui.leaderboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.databinding.FragmentLeaderboardBinding;
import com.gigamind.cognify.ui.OnboardingActivity;

/**
 * A fragment that displays the top‐100 leaderboard.  If the user is not signed in,
 * we only show a “Sign In” prompt.  Once signed in, we fetch the leaderboard once
 * and keep it in a ViewModel (avoiding flicker on subsequent visits).
 */
public class LeaderboardFragment extends Fragment {
    private FragmentLeaderboardBinding binding;
    private LeaderboardAdapter adapter;
    private FirebaseService firebaseService;
    private LeaderboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseService = FirebaseService.getInstance();
        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);

        setupRecyclerView();
        setupListeners();
        observeViewModel();
        checkAndLoad();
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        // Pull‐to‐refresh always forces a refresh.
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (firebaseService.isUserSignedIn()) {
                viewModel.refresh();
            } else {
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        // Retry button in case of error:
        binding.retryButton.setOnClickListener(v -> {
            checkAndLoad();
        });

        // If user taps “Sign In,” we send them to OnboardingActivity (or your login screen)
        binding.signInButton.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), OnboardingActivity.class));
        });
    }

    private void observeViewModel() {
        // Observe the LiveData in our ViewModel.  Whenever it changes:
        viewModel.leaderboard.observe(getViewLifecycleOwner(), items -> {
            // Hide all overlays first:
            hideAllStateViews();

            if (!firebaseService.isUserSignedIn()) {
                // If no one is signed in, show the sign-in prompt:
                binding.signInPrompt.setVisibility(View.VISIBLE);
                binding.swipeRefresh.setRefreshing(false);
                return;
            }

            if (items == null) {
                // items == null means “still loading” OR “error.”  But we can’t distinguish them easily,
                // so just show the spinner for now.  If it really is an error, viewModel will never populate.
                binding.swipeRefresh.setRefreshing(true);
            } else {
                // Data arrived (could be empty list, or real items).
                binding.swipeRefresh.setRefreshing(false);
                if (items.isEmpty()) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                } else {
                    binding.leaderboard.setVisibility(View.VISIBLE);
                    adapter.submitList(items);
                }
            }
        });
    }

    private void checkAndLoad() {
        // Called in onViewCreated and also on “retry”.  If user not signed in, show sign‐in prompt.
        if (!firebaseService.isUserSignedIn()) {
            hideAllStateViews();
            binding.signInPrompt.setVisibility(View.VISIBLE);
            binding.swipeRefresh.setRefreshing(false);
        } else {
            // User is signed in.  If we already have data in viewModel, the observer will update UI at once.
            // Otherwise, tell ViewModel to load from Firestore once.
            // (ViewModel.loadOnce() only actually runs a query if no data is present.)
            hideAllStateViews();
            viewModel.loadOnce();
        }
    }

    private void hideAllStateViews() {
        binding.swipeRefresh.setRefreshing(false);
        binding.leaderboard.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.GONE);
        binding.errorView.setVisibility(View.GONE);
        binding.signInPrompt.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
