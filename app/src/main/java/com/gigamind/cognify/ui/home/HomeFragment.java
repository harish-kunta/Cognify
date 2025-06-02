package com.gigamind.cognify.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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
import com.gigamind.cognify.databinding.FragmentHomeBinding;
import com.gigamind.cognify.databinding.MathGameCardBinding;
import com.gigamind.cognify.databinding.WordGameCardBinding;
import com.gigamind.cognify.ui.WordDashActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    private TextView dailyChallengeTitle;
    private CardView playWordDashButton;
    private CardView playQuickMathButton;
    private RelativeLayout cardView;

    private TextView  streakCount;

    private SharedPreferences prefs;

    // Firestore user
    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews();

        // Initialize SharedPreferences
        prefs = getContext().getSharedPreferences("GamePrefs", MODE_PRIVATE);

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // 4) Populate the streak UI
        loadAndDisplayStreak();

        // Set up daily challenge
        setupDailyChallenge();

        setupClickListeners();
    }

    private void initializeViews() {
        dailyChallengeTitle = binding.dailyChallengeTitle;
        playWordDashButton = binding.wordGameCard.getRoot();
        playQuickMathButton = binding.mathGameCard.getRoot();
        cardView = binding.cardView;
        streakCount = binding.streakCount;
    }

    private void loadAndDisplayStreak() {
        // If logged in, attempt Firestore read:
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            firestore.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists() && snapshot.contains("currentStreak")) {
                            long streakFromFs = snapshot.getLong("currentStreak");
                            int streakValue = (int) streakFromFs;

                            // Display it
                            streakCount.setText(String.valueOf(streakValue));

                            // Also write it back into SharedPreferences so local cache updates
                            prefs.edit().putInt("current_streak", streakValue).apply();
                        } else {
                            // Document exists but no field → fallback
                            fallbackStreakDisplay();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Firestore read failed → fallback
                        fallbackStreakDisplay();
                    });
        } else {
            // Not signed in: just read local
            fallbackStreakDisplay();
        }
    }

    private void fallbackStreakDisplay() {
        int localStreak = prefs.getInt("current_streak", 0);
        streakCount.setText(String.valueOf(localStreak));
    }

    private void setupDailyChallenge() {
        // Determine today's challenge type based on day of week
        Calendar calendar = Calendar.getInstance();
        boolean isWordDay = calendar.get(Calendar.DAY_OF_WEEK) % 2 == 0;

        String challengeType = isWordDay ? "Word Dash" : "Quick Math";
        dailyChallengeTitle.setText(challengeType);

        // Check if daily challenge is already completed
        String today = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);
        boolean isDailyCompleted = prefs.getBoolean("daily_completed_" + today, false);

        if (isDailyCompleted) {
            dailyChallengeTitle.setText("Completed Today");
            dailyChallengeTitle.setEnabled(false);
        }
    }

    private void setupClickListeners() {
        View.OnClickListener animatedClickListener = v -> {

            // Apply bounce animation
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.button_bounce));

            // Handle click after animation delay
            v.postDelayed(() -> handleGameLaunch(v), 200);
        };

        dailyChallengeTitle.setOnClickListener(animatedClickListener);
        playWordDashButton.setOnClickListener(animatedClickListener);
        playQuickMathButton.setOnClickListener(animatedClickListener);
        cardView.setOnClickListener(animatedClickListener);
    }

    private void handleGameLaunch(View v) {
        Intent intent = new Intent(getContext(), WordDashActivity.class);
        boolean isDaily = v.getId() == R.id.cardView;

        if (v.getId() == R.id.wordGameCard ||
                (isDaily && dailyChallengeTitle.getText().toString().equals("Word Dash"))) {
            intent.putExtra("GAME_TYPE", "WORD");
        } else {
            intent.putExtra("GAME_TYPE", "MATH");
        }

        intent.putExtra("IS_DAILY_CHALLENGE", isDaily);
        startActivity(intent);

        // If it's a daily challenge, mark it as completed
        if (isDaily) {
            Calendar calendar = Calendar.getInstance();
            String today = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_YEAR);
            prefs.edit().putBoolean("daily_completed_" + today, true).apply();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 