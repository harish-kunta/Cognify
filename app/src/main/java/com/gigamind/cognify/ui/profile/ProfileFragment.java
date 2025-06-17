package com.gigamind.cognify.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import de.hdodenhof.circleimageview.CircleImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gigamind.cognify.R;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.util.Constants;
import com.gigamind.cognify.util.AvatarLoader;
import com.gigamind.cognify.util.UserFields;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.DateFormat;
import java.util.Date;

/**
 * ProfileFragment now uses a real-time Firestore listener (attachUserDocumentListener),
 * so that any cached values appear immediately, and only the “server update” happens once.
 * This eliminates any flicker of a stale “0” or “1” on screen.
 */
public class ProfileFragment extends Fragment {
    private TextView userNameText;
    private TextView userEmailText;
    private TextView userJoinedText;
    private ImageView settingsIcon;
    private TextView streakValueText;
    private TextView xpValueText;
    private TextView gamesPlayedValue;
    private TextView winRateValue;
    private Button inviteFriendsButton;
    private Button shareStreakButton;
    private Button trophyRoomButton;
    private CircleImageView profileAvatar;

    private UserRepository userRepository;
    private FirebaseUser firebaseUser;
    private ListenerRegistration userDocListener;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Bind all views
        userNameText        = view.findViewById(R.id.userName);
        userEmailText       = view.findViewById(R.id.userEmail);
        userJoinedText      = view.findViewById(R.id.userJoined);
        settingsIcon        = view.findViewById(R.id.settingsIcon);
        streakValueText     = view.findViewById(R.id.streakValue);
        xpValueText         = view.findViewById(R.id.xpValue);
        gamesPlayedValue    = view.findViewById(R.id.gamesPlayedValue);
        winRateValue        = view.findViewById(R.id.winRateValue);
        inviteFriendsButton = view.findViewById(R.id.inviteFriendsButton);
        trophyRoomButton        = view.findViewById(R.id.trophyRoomButton);
        shareStreakButton       = view.findViewById(R.id.shareStreakButton);
        profileAvatar           = view.findViewById(R.id.profileAvatar);

        // 2) Initialize FirebaseUser & UserRepository
        firebaseUser   = FirebaseService.getInstance().getCurrentUser();
        userRepository = new UserRepository(requireContext());

        // 3) Populate static profile info (name, email, joined date)
        populateUserInfo();

        // Load avatar initially
        loadAvatar();

        // 4) Set up “overview” tiles (streak & XP) via real-time listener
        attachRealtimeOverviewListener();

        // 5) Hook up click listeners
        setupClickListeners();
    }

    private void populateUserInfo() {
        if (firebaseUser != null) {
            // Display name & email
            String displayName = firebaseUser.getDisplayName();
            String email       = firebaseUser.getEmail();
            userNameText.setText(displayName != null ? displayName : getString(R.string.unknown_user));
            userEmailText.setText(email != null ? email : "");

            // “Joined” date from FirebaseUser metadata
            long creationTs = firebaseUser.getMetadata() != null
                    ? firebaseUser.getMetadata().getCreationTimestamp()
                    : 0L;
            if (creationTs > 0) {
                String joinedDate = DateFormat.getDateInstance().format(new Date(creationTs));
                userJoinedText.setText(getString(R.string.joined_prefix, joinedDate));
            } else {
                userJoinedText.setText("");
            }
        } else {
            // Guest user
            userNameText.setText(getString(R.string.guest));
            userEmailText.setText("");
            userJoinedText.setText("");
        }
    }

    private void attachRealtimeOverviewListener() {
        // 1) Start with blank values—so nothing flickers before we get “cache or server” data
        streakValueText.setText("");
        xpValueText.setText("");
        gamesPlayedValue.setText("");
        winRateValue.setText("");

        if (firebaseUser != null) {
            // 2) Attach the Firestore listener
            userDocListener = userRepository.attachUserDocumentListener(new UserRepository.OnUserDataChanged() {
                @Override
                public void onDataChanged() {
                    // Firestore just updated SharedPreferences; read from there:
                    final int streak = userRepository.getCurrentStreak();
                    final int totalXp = userRepository.getTotalXP();
                    final int games = userRepository.getTotalGames();
                    final int wins = userRepository.getTotalWins();
                    final int losses = userRepository.getTotalLosses();
                    final String encodedPic = userRepository.getProfilePicture();

                    // Always update UI on main thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            streakValueText.setText(String.valueOf(streak));
                            xpValueText.setText(String.valueOf(totalXp));
                            gamesPlayedValue.setText(String.valueOf(games));
                            int total = wins + losses;
                            String rate = total > 0 ? (100 * wins / total) + "%" : "0%";
                            winRateValue.setText(rate);
                            if (encodedPic != null && !encodedPic.isEmpty()) {
                                AvatarLoader.load(userRepository, profileAvatar);
                            }
                        });
                    }
                }
            });
        } else {
            // 3) If not signed in, simply read local prefs once
            int streak  = userRepository.getCurrentStreak();
            int totalXp = userRepository.getTotalXP();
            int games   = userRepository.getTotalGames();
            int wins    = userRepository.getTotalWins();
            int losses  = userRepository.getTotalLosses();
            streakValueText.setText(String.valueOf(streak));
            xpValueText.setText(String.valueOf(totalXp));
            gamesPlayedValue.setText(String.valueOf(games));
            int total = wins + losses;
            String rate = total > 0 ? (100 * wins / total) + "%" : "0%";
            winRateValue.setText(rate);
            loadAvatar();
        }
    }

    private void setupClickListeners() {
        // (Alternatively, open a real SettingsActivity)
        settingsIcon.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
            bottomNav.setSelectedItemId(R.id.navigation_settings);
        });

        inviteFriendsButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String friendCode = (firebaseUser != null) ? firebaseUser.getUid() : "guest";
            String message = getString(R.string.invite_message) + "\nCode: " + friendCode;
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.invite_chooser_title)));
        });


        shareStreakButton.setOnClickListener(v -> {
            int streak = userRepository.getCurrentStreak();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,
                    getString(R.string.share_streak_message, streak));
            startActivity(Intent.createChooser(intent, getString(R.string.invite_chooser_title)));
        });

        trophyRoomButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.gigamind.cognify.ui.trophy.TrophyRoomActivity.class);
            startActivity(intent);
        });
        profileAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.gigamind.cognify.ui.avatar.AvatarMakerActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAvatar();
    }

    private void loadAvatar() {
        AvatarLoader.load(userRepository, profileAvatar);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Always remove the Firestore listener to prevent memory leaks
        if (userDocListener != null) {
            userDocListener.remove();
            userDocListener = null;
        }
    }
}
