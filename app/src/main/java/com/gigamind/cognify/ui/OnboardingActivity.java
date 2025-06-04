package com.gigamind.cognify.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gigamind.cognify.R;
import com.gigamind.cognify.adapter.OnboardingAdapter;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.databinding.ActivityOnboardingBinding;
import com.gigamind.cognify.util.OnboardingItem;
import com.gigamind.cognify.util.UserFields;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OnboardingActivity now also asks for notification permission during onboarding.
 * We show a rationale dialog until the user either grants the permission
 * (Android 13+) or explicitly taps “No thanks” to decline.
 */
public class OnboardingActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private static final String PREFS_NAME = "GamePrefs";
    private static final String PREF_ASKED_NOTIFICATIONS = "asked_for_notifications";

    private ActivityOnboardingBinding binding;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private SharedPreferences prefs;

    // Launcher for the new Android 13+ notification permission request
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Configure Google Sign In (web client ID stored in strings.xml)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // 1) Set up the permission launcher (Android 13+)
        setupNotificationPermissionLauncher();

        // 2) Immediately check if we should ask for notification permission
        checkAndAskNotificationPermissionIfNeeded();

        // 3) Proceed with the rest of onboarding
        setupOnboarding();
        setupButtons();
    }

    /**
     * Configure the ActivityResultLauncher for the POST_NOTIFICATIONS permission (API 33+).
     */
    private void setupNotificationPermissionLauncher() {
        requestNotificationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                // User granted POST_NOTIFICATIONS; no further action needed
                                Toast.makeText(this, "Notifications enabled. You won’t lose your streak!", Toast.LENGTH_SHORT).show();
                            } else {
                                // User denied. We’ll treat this as “No thanks” and stop asking again.
                                prefs.edit()
                                        .putBoolean(PREF_ASKED_NOTIFICATIONS, true)
                                        .apply();
                                Toast.makeText(this, "Notifications disabled. You may lose your streak if you don't play.", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
    }

    /**
     * If notifications aren’t enabled yet, and we haven’t already asked, show a rationale dialog.
     * This will loop until the user either grants permission or taps “No thanks.”
     */
    private void checkAndAskNotificationPermissionIfNeeded() {
        // If device < Android 13, no runtime permission needed—return early.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        // If user already tapped “No thanks,” we don’t ask again.
        boolean alreadyAsked = prefs.getBoolean(PREF_ASKED_NOTIFICATIONS, false);
        if (alreadyAsked) {
            return;
        }

        // If permission is already granted, nothing to do.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Otherwise, show a custom rationale dialog explaining why we need notifications:
        new AlertDialog.Builder(this)
                .setTitle("Keep Your Streak Alive!")
                .setMessage("We’d like to send you a daily reminder so you won’t lose your hard-earned streak. " +
                        "Allow notifications to receive gentle nudges if you haven’t played today.")
                .setPositiveButton("Enable", (dialog, which) -> {
                    // Launch the system permission prompt
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                })
                .setNegativeButton("No thanks", (dialog, which) -> {
                    // User does not want notifications: record that and close dialog
                    prefs.edit()
                            .putBoolean(PREF_ASKED_NOTIFICATIONS, true)
                            .apply();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void setupOnboarding() {
        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem(
                R.drawable.brain_train,
                "Train your brain in 60 seconds a day",
                "Quick, fun exercises to keep your mind sharp"
        ));
        items.add(new OnboardingItem(
                R.drawable.word_math,
                "Form words. Solve math. Beat the clock!",
                "Challenge yourself with various brain games"
        ));
        items.add(new OnboardingItem(
                R.drawable.rewards,
                "Earn streaks, top leaderboards!",
                "Compete and track your progress"
        ));
        items.add(new OnboardingItem(
                R.drawable.profile,
                "Sign in to save progress",
                "Or continue as guest to try it out"
        ));

        OnboardingAdapter adapter = new OnboardingAdapter(items);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(
                binding.tabLayout,
                binding.viewPager,
                (tab, position) -> { /* no custom tab text */ }
        ).attach();

        binding.tabLayout.post(() -> {
            ViewGroup tabStrip = (ViewGroup) binding.tabLayout.getChildAt(0);
            for (int i = 0; i < tabStrip.getChildCount(); i++) {
                View tabView = tabStrip.getChildAt(i);
                ViewGroup.LayoutParams lp = tabView.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                tabView.setLayoutParams(lp);

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tabView.getLayoutParams();
                params.setMargins(12, 0, 12, 0);
                tabView.requestLayout();
            }
        });
    }

    private void setupButtons() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Already signed in
            binding.btnSignIn.setVisibility(View.GONE);
            binding.btnContinueAsGuest.setVisibility(View.GONE);

            binding.btnLetsGo.setVisibility(View.VISIBLE);
            binding.btnLetsGo.setOnClickListener(v -> {
                Intent i = new Intent(OnboardingActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            });
        } else {
            // Not signed in
            binding.btnLetsGo.setVisibility(View.GONE);

            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.btnSignIn.setOnClickListener(v -> signIn());

            binding.btnContinueAsGuest.setVisibility(View.VISIBLE);
            binding.btnContinueAsGuest.setOnClickListener(v -> continueAsGuest());
        }
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void continueAsGuest() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken(), account);
                }
            } catch (ApiException e) {
                Toast.makeText(this,
                        "Google sign-in failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount gAccount) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        createOrUpdateUserInFirestore(user, gAccount);
                    } else {
                        Toast.makeText(this,
                                "Authentication succeeded but no user found.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Firebase authentication failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Writes or merges the newly signed-in user’s data into Firestore.
     * Uses UserFields constants for all field names.
     */
    private void createOrUpdateUserInFirestore(FirebaseUser user, GoogleSignInAccount gAccount) {
        String uid = user.getUid();
        String name = gAccount.getDisplayName();
        String email = gAccount.getEmail();

        Map<String, Object> userData = new HashMap<>();
        userData.put(UserFields.FIELD_UID, uid);
        userData.put(UserFields.FIELD_NAME, (name != null ? name : ""));
        userData.put(UserFields.FIELD_EMAIL, (email != null ? email : ""));
        userData.put(UserFields.FIELD_SCORE, 0);
        userData.put(UserFields.FIELD_CURRENT_STREAK, 0);
        userData.put(UserFields.FIELD_LEADERBOARD_RANK, 0);
        userData.put(UserFields.FIELD_TROPHIES, new ArrayList<>());

        DocumentReference userRef = firestore
                .collection(FirebaseService.COLLECTION_USERS)
                .document(uid);

        userRef.set(userData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OnboardingActivity.this,
                            "Failed to save user data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // If the user manually taps “back,” we might want to re-ask next time, so do not override onBackPressed here.
}
