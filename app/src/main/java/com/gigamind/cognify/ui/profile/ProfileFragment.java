package com.gigamind.cognify.ui.profile;

import android.content.Intent;
import android.net.Uri;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DateFormat;
import java.util.Date;

/**
 * ProfileFragment populates user information (name, email, joined date)
 * and overview tiles (streak and total XP) via UserRepository and FirebaseService.
 */
public class ProfileFragment extends Fragment {
    private TextView userNameText;
    private TextView userEmailText;
    private TextView userJoinedText;
    private ImageView profileImageView;
    private ImageButton editProfileImageButton;
    private ImageView settingsIcon;
    private TextView streakValueText;
    private TextView xpValueText;
    private Button inviteFriendsButton;

    private UserRepository userRepository;
    private FirebaseUser firebaseUser;

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

        // 1) Bind views
        userNameText             = view.findViewById(R.id.userName);
        userEmailText            = view.findViewById(R.id.userEmail);
        userJoinedText           = view.findViewById(R.id.userJoined);
        profileImageView         = view.findViewById(R.id.profileImageView);
        editProfileImageButton   = view.findViewById(R.id.editProfileImageButton);
        settingsIcon             = view.findViewById(R.id.settingsIcon);
        streakValueText          = view.findViewById(R.id.streakValue);
        xpValueText              = view.findViewById(R.id.xpValue);
        inviteFriendsButton      = view.findViewById(R.id.inviteFriendsButton);

        // 2) Initialize FirebaseUser and UserRepository
        firebaseUser = FirebaseService.getInstance().getCurrentUser();
        userRepository = new UserRepository(requireContext());

        // 3) Populate profile fields
        populateUserInfo();

        // 4) Populate overview tiles (streak & XP)
        populateOverview();

        // 5) Hook up click listeners
        setupClickListeners();
    }

    private void populateUserInfo() {
        if (firebaseUser != null) {
            // Display name and email
            String displayName = firebaseUser.getDisplayName();
            String email       = firebaseUser.getEmail();
            userNameText.setText(displayName != null ? displayName : "Unknown User");
            userEmailText.setText(email != null ? email : "");

            // Joined date from FirebaseUser metadata
            long creationTs = firebaseUser.getMetadata() != null
                    ? firebaseUser.getMetadata().getCreationTimestamp()
                    : 0;
            if (creationTs > 0) {
                String joinedDate = DateFormat.getDateInstance().format(new Date(creationTs));
                userJoinedText.setText("Joined " + joinedDate);
            } else {
                userJoinedText.setText("");
            }

            // TODO: Load profileImageView from a URL if stored (omitted here)
        } else {
            // Guest user
            userNameText.setText("Guest");
            userEmailText.setText("");
            userJoinedText.setText("");
            profileImageView.setImageResource(R.drawable.ic_avatar); // a placeholder
        }
    }

    private void populateOverview() {
        if (firebaseUser != null) {
            // Sync remote data into local prefs first
            Task<DocumentSnapshot> syncTask = userRepository.syncUserData();
            if (syncTask != null) {
                syncTask.addOnSuccessListener(snapshot -> {
                    displayStreakAndXp();
                }).addOnFailureListener(e -> {
                    // Show local fallback and notify
                    displayStreakAndXp();
                    Snackbar.make(
                            requireView(),
                            "Unable to fetch latest data. Showing cached values.",
                            Snackbar.LENGTH_LONG
                    ).show();
                });
                return;
            }
        }
        // Not signed in or syncTask == null: just display local values
        displayStreakAndXp();
    }

    private void displayStreakAndXp() {
        int streak = userRepository.getCurrentStreak();
        int totalXp = userRepository.getTotalXP();

        streakValueText.setText(String.valueOf(streak));
        xpValueText.setText(String.valueOf(totalXp));
    }

    private void setupClickListeners() {
        // Open settings when the gear icon is tapped
//        settingsIcon.setOnClickListener(v -> {
//            // TODO: Replace with actual settings activity
//            Intent intent = new Intent(requireContext(), SettingsActivity.class);
//            startActivity(intent);
//        });

        editProfileImageButton.setOnClickListener(v -> {
            // TODO: Open image picker or profile edit UI
            Toast.makeText(requireContext(), "Edit profile picture", Toast.LENGTH_SHORT).show();
        });

        inviteFriendsButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Join me on Cognify! Download the app: https://example.com/app"
            );
            startActivity(Intent.createChooser(shareIntent, "Invite Friends"));
        });
    }
}
