package com.gigamind.cognify.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gigamind.cognify.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import de.hdodenhof.circleimageview.CircleImageView;

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
import com.gigamind.cognify.util.AvatarLoader;
import com.gigamind.cognify.util.DailyChallengeManager;
import android.graphics.Color;
import android.content.res.ColorStateList;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;


/**
 * HomeFragment now uses UserRepository to fetch and display the user's streak,
 * instead of calling Firestore directly.
 */
public class HomeFragment extends Fragment {
    MaterialButton wordGamePlayButton;
    MaterialButton quickMathPlayButton;
    private FragmentHomeBinding binding;
    private TextView dailyChallengeTitle;
    private TextView dailyChallengeGame;
    private CardView playWordDashButton;
    private CardView playQuickMathButton;
    private RelativeLayout cardView;
    private TextView streakCount;
    private TextView dailyPerk;
    private CircleImageView currentUserAvatar;
    private ImageView dailyChallengeLogo;
    /** Flag cached from setupDailyChallenge to know which game is today's challenge */
    private boolean isWordDay;
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
        com.gigamind.cognify.animation.AnimatorProvider.updateFromPreferences(requireContext());
        userRepository = new UserRepository(requireContext());

        // Check signed-in user
        firebaseUser = FirebaseService.getInstance().getCurrentUser();

        // 1) Populate the streak UI
        loadAndDisplayStreak();
        loadAvatar();

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
        dailyChallengeGame = binding.dailyChallengeGame;
        playWordDashButton = binding.wordGameCard.getRoot();
        playQuickMathButton = binding.mathGameCard.getRoot();
        cardView = binding.welcomeCardView;
        streakCount = binding.streakCount;
        dailyPerk = binding.dailyPerk;
        currentUserAvatar = binding.currentUserAvatar;
        dailyChallengeLogo = binding.dailyChallengeLogo;
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

    private void loadAvatar() {
        AvatarLoader.load(userRepository, currentUserAvatar);
    }

    /**
     * Determines and displays today's challenge ("Word Dash" or "Quick Math"),
     * then disables it if already completed today (based on local prefs).
     */
    private void setupDailyChallenge() {
        // Determine todays challenge and perk via helper
        String type = DailyChallengeManager.getTodayType(requireContext());
        isWordDay = Constants.GAME_TYPE_WORD.equals(type);
        String challengeType = isWordDay
                ? getString(R.string.word_dash)
                : getString(R.string.quick_math);
        dailyChallengeTitle.setText(R.string.daily_challenge);
        dailyChallengeGame.setText(getString(R.string.today_challenge_format, challengeType));
        dailyChallengeLogo.setImageResource(
                isWordDay ? R.drawable.ic_word_logo : R.drawable.ic_math_logo);

        String perk = DailyChallengeManager.getTodayPerk(requireContext());
        dailyPerk.setText(getString(R.string.perk_format, perk));

        boolean isDailyCompleted = DailyChallengeManager.isCompleted(requireContext());

        if (isDailyCompleted) {
            dailyChallengeTitle.setText(getString(R.string.completed_today));
        }
    }

    /** Sets titles, backgrounds and button colors for the game cards. */
    private void setupGameCards() {
        // Word Dash card
        binding.wordGameCard.cardTitle.setText(getString(R.string.word_dash));
        binding.wordGameCard.container.setBackgroundResource(R.drawable.bg_card_word);
        binding.wordGameCard.logoImage.setImageResource(R.drawable.ic_word_logo);

        // Quick Math card overrides default accent color
        binding.mathGameCard.cardTitle.setText(getString(R.string.quick_math));
        binding.mathGameCard.container.setBackgroundResource(R.drawable.bg_card_math);
        binding.mathGameCard.logoImage.setImageResource(R.drawable.ic_math_logo);
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
            if (areAnimationsEnabled()) {
                AnimationUtils.bounce(v);
            }
            handleGameLaunch(v);
        };

        dailyChallengeTitle.setOnClickListener(animatedClickListener);
        dailyChallengeGame.setOnClickListener(animatedClickListener);
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
        boolean isDaily = (v.getId() == R.id.welcomeCardView
                || v.getId() == R.id.dailyChallengeTitle
                || v.getId() == R.id.dailyChallengeGame);
        if (isDaily && DailyChallengeManager.isCompleted(requireContext())) {
            Snackbar.make(binding.getRoot(),
                    getString(R.string.daily_completed_msg),
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        boolean openWordDash;
        if (v.getId() == R.id.wordGameCard || v == binding.wordGameCard.playButton) {
            openWordDash = true;
        } else if (v.getId() == R.id.mathGameCard || v == binding.mathGameCard.playButton) {
            openWordDash = false;
        } else {
            // Daily challenge card or title - follow today's designated game
            openWordDash = isWordDay;
        }

        Intent intent;
        if (openWordDash) {
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
        if (homeListener != null) {
            homeListener.remove();
        }
    }
}
