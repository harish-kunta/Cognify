package com.gigamind.cognify.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gigamind.cognify.R;
import com.gigamind.cognify.databinding.FragmentHomeBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private Handler timerHandler;
    private Runnable timerRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up streak
        int streak = requireActivity().getSharedPreferences("AppPrefs", 0).getInt("streak", 0);
        binding.streakText.setText(getString(R.string.streak_count, streak));

        // Set up daily challenge card
        setupDailyChallenge();

        // Set up timer for next challenge
        setupNextChallengeTimer();
    }

    private void setupDailyChallenge() {
        binding.challengeCard.setOnClickListener(v -> {
            // Start daily challenge activity
            // TODO: Implement challenge activity navigation
        });

        // Animate card on touch
        binding.challengeCard.setOnTouchListener((v, event) -> {
            MaterialCardView card = (MaterialCardView) v;
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    card.setCardElevation(0f);
                    card.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    card.setCardElevation(8f);
                    card.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    private void setupNextChallengeTimer() {
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                Calendar nextDay = Calendar.getInstance();
                nextDay.add(Calendar.DAY_OF_MONTH, 1);
                nextDay.set(Calendar.HOUR_OF_DAY, 0);
                nextDay.set(Calendar.MINUTE, 0);
                nextDay.set(Calendar.SECOND, 0);

                long diff = nextDay.getTimeInMillis() - now.getTimeInMillis();
                String timeLeft = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(diff),
                        TimeUnit.MILLISECONDS.toMinutes(diff) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(diff) % 60);

                binding.nextChallengeText.setText(getString(R.string.next_challenge, timeLeft));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        binding = null;
    }
} 