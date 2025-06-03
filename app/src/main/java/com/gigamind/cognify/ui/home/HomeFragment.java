package com.gigamind.cognify.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.gigamind.cognify.R;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.databinding.FragmentHomeBinding;
import com.gigamind.cognify.ui.WordDashActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * HomeFragment now uses UserRepository to fetch and display the user's streak,
 * instead of calling Firestore directly.
 */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    private TextView dailyChallengeTitle;
    private CardView playWordDashButton;
    private CardView playQuickMathButton;
    private RelativeLayout cardView;
    private TextView streakCount;

    private SharedPreferences prefs;
    private UserRepository userRepository;
    private FirebaseUser firebaseUser;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews();

        // Initialize SharedPreferences and UserRepository
        prefs = requireContext().getSharedPreferences("GamePrefs", MODE_PRIVATE);
        userRepository = new UserRepository(requireContext());

        // Check signed-in user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // 1) Populate the streak UI
        loadAndDisplayStreak();

        // 2) Set up daily challenge text
        setupDailyChallenge();

        // 3) Set up click listeners on UI
        setupClickListeners();
    }

    /**
     * Binds view references from the layout.
     */
    private void initializeViews() {
        dailyChallengeTitle = binding.dailyChallengeTitle;
        playWordDashButton  = binding.wordGameCard.getRoot();
        playQuickMathButton = binding.mathGameCard.getRoot();
        cardView            = binding.cardView;
        streakCount         = binding.streakCount;
    }

    /**
     * Fetches (and caches) the remote streak if signed-in, then displays it.
     * Falls back to local if not signed-in or network fails.
     */
    private void loadAndDisplayStreak() {
        // If user is signed in, sync from Firestore into local prefs first
        if (firebaseUser != null) {
            userRepository.syncUserData()
                    .addOnSuccessListener(snapshot -> {
                        // Once sync succeeds (or even if the document doesn't exist),
                        // read the local value (which was just updated).
                        int streakValue = userRepository.getCurrentStreak();
                        streakCount.setText(String.valueOf(streakValue));
                    })
                    .addOnFailureListener(e -> {
                        // If syncing fails, fallback to purely local
                        int localStreak = userRepository.getCurrentStreak();
                        streakCount.setText(String.valueOf(localStreak));
                        Snackbar.make(
                                binding.getRoot(),
                                "Could not load streak from server. Showing local value.",
                                Snackbar.LENGTH_LONG
                        ).show();
                    });
        } else {
            // Not signed in: read local-only streak from prefs
            int localStreak = userRepository.getCurrentStreak();
            streakCount.setText(String.valueOf(localStreak));
        }
    }

    /**
     * Determines and displays today's challenge ("Word Dash" or "Quick Math"),
     * then disables it if already completed today (based on local prefs).
     */
    private void setupDailyChallenge() {
        Calendar calendar = Calendar.getInstance();
        boolean isWordDay = (calendar.get(Calendar.DAY_OF_WEEK) % 2) == 0;
        String challengeType = isWordDay ? "Word Dash" : "Quick Math";
        dailyChallengeTitle.setText(challengeType);

        // Use a consistent "YYYY-DDD" key in prefs to mark completion
        String todayKey = new SimpleDateFormat("yyyy-DDD", Locale.US).format(calendar.getTime());
        boolean isDailyCompleted = prefs.getBoolean("daily_completed_" + todayKey, false);

        if (isDailyCompleted) {
            dailyChallengeTitle.setText("Completed Today");
            dailyChallengeTitle.setEnabled(false);
        }
    }

    /**
     * Sets up bounce animation and routing to the appropriate game
     * when the user clicks any of the cards or the daily challenge banner.
     */
    private void setupClickListeners() {
        View.OnClickListener animatedClickListener = v -> {
            // 1) Bounce animation
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.button_bounce));
            // 2) Delay to let the animation run, then launch game
            v.postDelayed(() -> handleGameLaunch(v), 200);
        };

        dailyChallengeTitle.setOnClickListener(animatedClickListener);
        playWordDashButton.setOnClickListener(animatedClickListener);
        playQuickMathButton.setOnClickListener(animatedClickListener);
        cardView.setOnClickListener(animatedClickListener);
    }

    /**
     * Decides which game to launch (WordDash or QuickMath) based on the view clicked
     * and whether it was the daily challenge. Also marks daily challenge complete.
     */
    private void handleGameLaunch(View v) {
        Intent intent = new Intent(getContext(), WordDashActivity.class);
        boolean isDaily = (v.getId() == R.id.cardView);

        if (v.getId() == R.id.wordGameCard ||
                (isDaily && "Word Dash".equals(dailyChallengeTitle.getText().toString()))) {
            intent.putExtra("GAME_TYPE", "WORD");
        } else {
            intent.putExtra("GAME_TYPE", "MATH");
        }
        intent.putExtra("IS_DAILY_CHALLENGE", isDaily);
        startActivity(intent);

        if (isDaily) {
            // Mark today's challenge as completed in prefs
            Calendar calendar = Calendar.getInstance();
            String todayKey = new SimpleDateFormat("yyyy-DDD", Locale.US)
                    .format(calendar.getTime());
            prefs.edit().putBoolean("daily_completed_" + todayKey, true).apply();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
