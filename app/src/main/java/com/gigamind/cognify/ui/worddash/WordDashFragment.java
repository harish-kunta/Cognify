package com.gigamind.cognify.ui.worddash;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gigamind.cognify.animation.AnimationUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gigamind.cognify.R;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.databinding.FragmentWordDashBinding;
import com.gigamind.cognify.ui.QuickMathActivity;
import com.gigamind.cognify.ui.WordDashActivity;
import com.gigamind.cognify.util.Constants;
import com.gigamind.cognify.util.AvatarLoader;
import com.gigamind.cognify.util.TutorialHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import com.gigamind.cognify.util.DailyChallengeManager;

/**
 * HomeFragment now uses UserRepository to fetch and display the user's streak,
 * instead of calling Firestore directly.
 */
public class WordDashFragment extends Fragment {
    MaterialButton wordGamePlayButton;
    private FragmentWordDashBinding binding;
    private TextView streakCount;
    private SharedPreferences prefs;
    private UserRepository userRepository;
    private FirebaseUser firebaseUser;
    private ListenerRegistration homeListener;
    private CircleImageView userProfileButton;
    private TutorialHelper tutorialHelper;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = FragmentWordDashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews();

        // Initialize SharedPreferences, repositories and helpers
        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        com.gigamind.cognify.animation.AnimatorProvider.updateFromPreferences(requireContext());
        userRepository = new UserRepository(requireContext());
        tutorialHelper = new TutorialHelper(requireContext());

        // Check signed-in user
        firebaseUser = FirebaseService.getInstance().getCurrentUser();

        // 1) Populate the streak UI
        loadAndDisplayStreak();
        loadAvatar();

        // 3) Set up click listeners on UI
        setupClickListeners();
    }

    /**
     * Binds view references from the layout.
     */
    private void initializeViews() {
        streakCount = binding.streakCount;
        wordGamePlayButton = binding.playWordDashButton;
        userProfileButton = binding.currentUserAvatar;
    }

    /**
     * Fetches (and caches) the remote streak if signed-in, then displays it.
     * Falls back to local if not signed-in or network fails.
     */
    private void loadAndDisplayStreak() {
        // If user is signed in, sync from Firestore into local prefs first
        if (firebaseUser != null) {
            homeListener = userRepository.attachUserDocumentListener(() -> {
                int streak = userRepository.getCurrentStreak();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> streakCount.setText(String.valueOf(streak)));
                }
            });
        } else {
            // Not signed in: read local-only streak from prefs
            int localStreak = userRepository.getCurrentStreak();
            streakCount.setText(String.valueOf(localStreak));
        }
    }

    private void loadAvatar() {
        AvatarLoader.load(userRepository, userProfileButton);
    }

    /**
     * Sets up bounce animation and routing to the appropriate game
     * when the user clicks any of the cards or the daily challenge banner.
     */
    private void setupClickListeners() {
        View.OnClickListener animatedClickListener = v -> {
            if (areAnimationsEnabled()) {
                AnimationUtils.bounce(v);
            }
            if (!tutorialHelper.isTutorialCompleted()) {
                String msg = getString(R.string.play_tip);
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
                binding.getRoot().announceForAccessibility(msg);
            }
            handleGameLaunch(v);
        };
        wordGamePlayButton.setOnClickListener(animatedClickListener);

        userProfileButton.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
            bottomNav.setSelectedItemId(R.id.navigation_profile);
        });
    }

    /**
     * Decides which game to launch (WordDash or QuickMath) based on the view clicked
     * and whether it was the daily challenge. Also marks daily challenge complete.
     */
    private void handleGameLaunch(View v) {
        boolean isDaily = (v.getId() == R.id.welcomeCardView);
        if (isDaily && DailyChallengeManager.isCompleted(requireContext())) {
            Snackbar.make(binding.getRoot(),
                    getString(R.string.daily_completed_msg),
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent intent;
        if (v.getId() == R.id.playWordDashButton) {
            intent = new Intent(getContext(), WordDashActivity.class);
            intent.putExtra(Constants.INTENT_GAME_TYPE, Constants.GAME_TYPE_WORD);
        } else {
            intent = new Intent(getContext(), QuickMathActivity.class);
            intent.putExtra(Constants.INTENT_GAME_TYPE, Constants.GAME_TYPE_MATH);
        }
        intent.putExtra(Constants.INTENT_IS_DAILY, isDaily);
        startActivity(intent);

        if (isDaily) {
            DailyChallengeManager.markCompleted(requireContext());
        }
    }

    private boolean areAnimationsEnabled() {
        return com.gigamind.cognify.animation.AnimatorProvider.isAnimationsEnabled();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
