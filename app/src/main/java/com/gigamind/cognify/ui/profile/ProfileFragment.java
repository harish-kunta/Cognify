package com.gigamind.cognify.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gigamind.cognify.R;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.data.firebase.FirebaseService;
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
    private Button inviteFriendsButton;

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
        userNameText            = view.findViewById(R.id.userName);
        userEmailText           = view.findViewById(R.id.userEmail);
        userJoinedText          = view.findViewById(R.id.userJoined);
        settingsIcon            = view.findViewById(R.id.settingsIcon);
        streakValueText         = view.findViewById(R.id.streakValue);
        xpValueText             = view.findViewById(R.id.xpValue);
        inviteFriendsButton     = view.findViewById(R.id.inviteFriendsButton);

        // 2) Initialize FirebaseUser & UserRepository
        firebaseUser   = FirebaseService.getInstance().getCurrentUser();
        userRepository = new UserRepository(requireContext());

        // 3) Populate static profile info (name, email, joined date)
        populateUserInfo();

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

            // TODO: if you store a profile‐picture URL, load it into ‘profileImageView’ here
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

        if (firebaseUser != null) {
            // 2) Attach the Firestore listener
            userDocListener = userRepository.attachUserDocumentListener(new UserRepository.OnUserDataChanged() {
                @Override
                public void onDataChanged() {
                    // Firestore just updated SharedPreferences; read from there:
                    final int streak = userRepository.getCurrentStreak();
                    final int totalXp = userRepository.getTotalXP();

                    // Always update UI on main thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            streakValueText.setText(String.valueOf(streak));
                            xpValueText.setText(String.valueOf(totalXp));
                        });
                    }
                }
            });
        } else {
            // 3) If not signed in, simply read local prefs once
            int streak  = userRepository.getCurrentStreak();
            int totalXp = userRepository.getTotalXP();
            streakValueText.setText(String.valueOf(streak));
            xpValueText.setText(String.valueOf(totalXp));
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
            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    getString(R.string.invite_message)
            );
            startActivity(Intent.createChooser(shareIntent, getString(R.string.invite_chooser_title)));
        });
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
