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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.gigamind.cognify.R;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.databinding.FragmentHomeBinding;
import com.gigamind.cognify.ui.QuickMathActivity;
import com.gigamind.cognify.ui.WordDashActivity;
import com.gigamind.cognify.util.Constants;
import android.graphics.Color;
import android.content.res.ColorStateList;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * HomeFragment now uses UserRepository to fetch and display the user's streak,
 * instead of calling Firestore directly.
 */
public class HomeFragment extends Fragment {
    MaterialButton wordGamePlayButton;
    MaterialButton quickMathPlayButton;
    private FragmentHomeBinding binding;
    private TextView dailyChallengeTitle;
    private CardView playWordDashButton;
    private CardView playQuickMathButton;
    private RelativeLayout cardView;
    private TextView streakCount;
    private SharedPreferences prefs;
    private UserRepository userRepository;
    private FirebaseUser firebaseUser;
    private ListenerRegistration homeListener;

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
        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userRepository = new UserRepository(requireContext());

        // Check signed-in user
        firebaseUser = FirebaseService.getInstance().getCurrentUser();

        // 1) Populate the streak UI
        loadAndDisplayStreak();

        // 2) Set up daily challenge text
        setupDailyChallenge();

        // 3) Configure game cards
        setupGameCards();

        // 4) Set up click listeners on UI
        setupClickListeners();
    }

    /**
     * Binds view references from the layout.
     */
    private void initializeViews() {
        dailyChallengeTitle = binding.dailyChallengeTitle;
        playWordDashButton = binding.wordGameCard.getRoot();
        playQuickMathButton = binding.mathGameCard.getRoot();
        cardView = binding.welcomeCardView;
        streakCount = binding.streakCount;
        wordGamePlayButton = binding.wordGameCard.playButton;
        quickMathPlayButton = binding.mathGameCard.playButton;
    }

    /**
     * Fetches (and caches) the remote streak if signed-in, then displays it.
     * Falls back to local if not signed-in or network fails.
     */
    private void loadAndDisplayStreak() {
        if (firebaseUser == null) {
            // Handle not signed in state
            streakCount.setText("0");
            binding.streakContainer.setVisibility(View.GONE);
            return;
        }

        binding.streakContainer.setVisibility(View.VISIBLE);

        userRepository.attachUserDocumentListener(() -> {
            int streak = userRepository.getCurrentStreak();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (isAdded() && getActivity() != null) {
                        streakCount.setText(String.valueOf(streak));
                    }
                });
            }
        });
    }

    /**
     * Determines and displays today's challenge ("Word Dash" or "Quick Math"),
     * then disables it if already completed today (based on local prefs).
     */
    private void setupDailyChallenge() {
        Calendar calendar = Calendar.getInstance();
        boolean isWordDay = (calendar.get(Calendar.DAY_OF_WEEK) % 2) == 0;
        String challengeType = isWordDay ? getString(R.string.word_dash) : getString(R.string.quick_math);
        dailyChallengeTitle.setText(challengeType);

        // Use a consistent "YYYY-DDD" key in prefs to mark completion
        String todayKey = new SimpleDateFormat("yyyy-DDD", Locale.US).format(calendar.getTime());
        boolean isDailyCompleted = prefs.getBoolean(Constants.PREF_DAILY_COMPLETED_PREFIX + todayKey, false);

        if (isDailyCompleted) {
            dailyChallengeTitle.setText(getString(R.string.completed_today));
            dailyChallengeTitle.setEnabled(false);
        }
    }

    /** Sets titles, backgrounds and button colors for the game cards. */
    private void setupGameCards() {
        // Word Dash card
        binding.wordGameCard.cardTitle.setText(getString(R.string.word_dash));
        binding.wordGameCard.container.setBackgroundResource(R.drawable.bg_card_word);

        // Quick Math card overrides default accent color
        binding.mathGameCard.cardTitle.setText(getString(R.string.quick_math));
        binding.mathGameCard.container.setBackgroundResource(R.drawable.bg_card_math);
        int mathColor = Color.parseColor("#B8860B");
        binding.mathGameCard.playButton.setBackgroundTintList(ColorStateList.valueOf(mathColor));
        binding.mathGameCard.playButton.setRippleColor(ColorStateList.valueOf(mathColor));
    }

    /**
     * Sets up bounce animation and routing to the appropriate game
     * when the user clicks any of the cards or the daily challenge banner.
     */
    private void setupClickListeners() {
        View.OnClickListener animatedClickListener = v -> {
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.button_bounce));
            handleGameLaunch(v);
        };

        dailyChallengeTitle.setOnClickListener(animatedClickListener);
        playWordDashButton.setOnClickListener(animatedClickListener);
        playQuickMathButton.setOnClickListener(animatedClickListener);
        wordGamePlayButton.setOnClickListener(animatedClickListener);
        quickMathPlayButton.setOnClickListener(animatedClickListener);
        cardView.setOnClickListener(animatedClickListener);
    }

    /**
     * Decides which game to launch (WordDash or QuickMath) based on the view clicked
     * and whether it was the daily challenge. Also marks daily challenge complete.
     */
    private void handleGameLaunch(View v) {
        boolean isDaily = (v.getId() == R.id.welcomeCardView);
        Intent intent;
        if (v.getId() == R.id.wordGameCard || v == binding.wordGameCard.playButton || v.getId() == R.id.welcomeCardView) {
            intent = new Intent(getContext(), WordDashActivity.class);
            intent.putExtra(Constants.INTENT_GAME_TYPE, Constants.GAME_TYPE_WORD);
        } else {
            intent = new Intent(getContext(), QuickMathActivity.class);
            intent.putExtra(Constants.INTENT_GAME_TYPE, Constants.GAME_TYPE_MATH);
        }
        intent.putExtra(Constants.INTENT_IS_DAILY, isDaily);
        startActivity(intent);

        if (isDaily) {
            // Mark today's challenge as completed in prefs
            Calendar calendar = Calendar.getInstance();
            String todayKey = new SimpleDateFormat("yyyy-DDD", Locale.US)
                    .format(calendar.getTime());
            prefs.edit().putBoolean(Constants.PREF_DAILY_COMPLETED_PREFIX + todayKey, true).apply();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (homeListener != null) {
            homeListener.remove();
        }
    }
}
