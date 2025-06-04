package com.gigamind.cognify.ui.worddash;

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
import com.gigamind.cognify.databinding.FragmentWordDashBinding;
import com.gigamind.cognify.ui.QuickMathActivity;
import com.gigamind.cognify.ui.WordDashActivity;
import com.google.android.material.button.MaterialButton;
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
public class WordDashFragment extends Fragment {
    MaterialButton wordGamePlayButton;
    private FragmentWordDashBinding binding;
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

        // Initialize SharedPreferences and UserRepository
        prefs = requireContext().getSharedPreferences("GamePrefs", MODE_PRIVATE);
        userRepository = new UserRepository(requireContext());

        // Check signed-in user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // 1) Populate the streak UI
        loadAndDisplayStreak();

        // 3) Set up click listeners on UI
        setupClickListeners();
    }

    /**
     * Binds view references from the layout.
     */
    private void initializeViews() {
        streakCount = binding.streakCount;
        wordGamePlayButton = binding.playWordDashButton;
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
     * Sets up bounce animation and routing to the appropriate game
     * when the user clicks any of the cards or the daily challenge banner.
     */
    private void setupClickListeners() {
        View.OnClickListener animatedClickListener = v -> {
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.button_bounce));
            handleGameLaunch(v);
        };
        wordGamePlayButton.setOnClickListener(animatedClickListener);
    }

    /**
     * Decides which game to launch (WordDash or QuickMath) based on the view clicked
     * and whether it was the daily challenge. Also marks daily challenge complete.
     */
    private void handleGameLaunch(View v) {
        boolean isDaily = (v.getId() == R.id.welcomeCardView);
        Intent intent;
        if (v.getId() == R.id.playWordDashButton) {
            intent = new Intent(getContext(), WordDashActivity.class);
            intent.putExtra("GAME_TYPE", "WORD");
        } else {
            intent = new Intent(getContext(), QuickMathActivity.class);
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
