package com.gigamind.cognify.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;


import androidx.appcompat.app.AlertDialog;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.material.snackbar.Snackbar;

import com.gigamind.cognify.ui.BaseActivity;

import com.gigamind.cognify.util.NotificationPermissionHelper;

import com.gigamind.cognify.R;
import com.gigamind.cognify.adapter.OnboardingAdapter;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.databinding.ActivityOnboardingBinding;
import com.gigamind.cognify.util.OnboardingItem;


import com.gigamind.cognify.util.ExceptionLogger;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gigamind.cognify.analytics.GameAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.gigamind.cognify.util.Constants;
import com.gigamind.cognify.util.SoundManager;
import com.gigamind.cognify.util.GoogleSignInHelper;


import java.util.ArrayList;
import java.util.List;

/**
 * OnboardingActivity now also asks for notification permission during onboarding.
 * We show a rationale dialog until the user either grants the permission
 * (Android 13+) or explicitly taps getString(R.string.no_thanks) to decline.
 */
public class OnboardingActivity extends BaseActivity {

    private ActivityOnboardingBinding binding;
    private FirebaseService firebaseService;
    private SharedPreferences prefs;
    private UserRepository userRepository;
    private NotificationPermissionHelper notificationPermissionHelper;
    private GameAnalytics analytics;
    private boolean onboardingCompleted = false;

    private void showLoading(boolean show) {
        binding.loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.buttonsContainer.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SoundManager.getInstance(this).playWelcome();

        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        analytics = GameAnalytics.getInstance(this);

        firebaseService = FirebaseService.getInstance();
        userRepository = new UserRepository(this);

        // 1) Configure notification permission helper (Android 13+)
        notificationPermissionHelper = new NotificationPermissionHelper(
                this,
                prefs,
                granted -> { /* no-op */ }
        );
        notificationPermissionHelper.requestIfNeeded();

        // 2) Proceed with the rest of onboarding
        setupOnboarding();
        setupButtons();
    }

    private void setupOnboarding() {
        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem.Builder()
                .imageResId(R.drawable.brain_train)
                .title(getString(R.string.onboarding_slide1_title))
                .description(getString(R.string.onboarding_slide1_desc))
                .build());
        items.add(new OnboardingItem.Builder()
                .imageResId(R.drawable.word_math)
                .title(getString(R.string.onboarding_slide2_title))
                .description(getString(R.string.onboarding_slide2_desc))
                .build());
        items.add(new OnboardingItem.Builder()
                .imageResId(R.drawable.rewards)
                .title(getString(R.string.onboarding_slide3_title))
                .description(getString(R.string.onboarding_slide3_desc))
                .build());
        items.add(new OnboardingItem.Builder()
                .imageResId(R.drawable.profile)
                .title(getString(R.string.onboarding_slide4_title))
                .description(getString(R.string.onboarding_slide4_desc))
                .build());

        OnboardingAdapter adapter = new OnboardingAdapter(items);
        binding.viewPager.setAdapter(adapter);

        analytics.logOnboardingPage(0);
        binding.viewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                analytics.logOnboardingPage(position);
                SoundManager.getInstance(OnboardingActivity.this).playSwipe();
            }
        });

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
        FirebaseUser currentUser = firebaseService.getCurrentUser();
        if (currentUser != null) {
            // Already signed in
            binding.btnSignIn.setVisibility(View.GONE);
            binding.btnContinueAsGuest.setVisibility(View.GONE);

            binding.btnLetsGo.setVisibility(View.VISIBLE);
            binding.btnLetsGo.setOnClickListener(v -> {
                SoundManager.getInstance(this).playButton();
                onboardingCompleted = true;
                analytics.logOnboardingCompleted();
                launchMainActivity();
            });
        } else {
            // Not signed in
            binding.btnLetsGo.setVisibility(View.GONE);

            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.btnSignIn.setOnClickListener(v -> {
                SoundManager.getInstance(this).playButton();
                signIn();
            });

            binding.btnContinueAsGuest.setVisibility(View.VISIBLE);
            binding.btnContinueAsGuest.setOnClickListener(v -> {
                SoundManager.getInstance(this).playButton();
                continueAsGuest();
            });
        }
    }

    private void signIn() {
        showLoading(true);
        GoogleSignInHelper.signIn(this, new GoogleSignInHelper.Callback() {
            @Override
            public void onSuccess(String idToken) {
                firebaseAuthWithGoogle(idToken);
            }

            @Override
            public void onError(Exception e) {
                ExceptionLogger.log("OnboardingActivity", e);
                showLoading(false);

                if (e instanceof NoCredentialException
                        || e.getMessage().contains("No credentials available")) {

                    // 1) NoCredentialException → disabled Google Sign-In
                    new AlertDialog.Builder(OnboardingActivity.this)
                            .setTitle("Google Sign-In Disabled")
                            .setMessage("It looks like Google Sign-In is turned off on your device. " +
                                    "How would you like to enable it?")
                            .setPositiveButton("Device instructions", (dlg, which) -> {
                                // show step-by-step in a second dialog
                                new AlertDialog.Builder(OnboardingActivity.this)
                                        .setTitle("Enable on your device")
                                        .setMessage(
                                                "1. Open **Settings**\n" +
                                                        "2. Tap **Google**\n" +
                                                        "3. Tap **Connected apps & services**\n" +
                                                        "4. Tap **Third-party apps & services**\n" +
                                                        "5. Tap the ⚙️ next to our app\n" +
                                                        "6. Enable **Login message**"
                                        )
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show();
                            })
                            .setNeutralButton("Cancel", null)
                            .show();

                } else if (e instanceof java.util.NoSuchElementException
                        || e.getMessage().toLowerCase().contains("no such element")) {

                    // 2) NoSuchElementException → no Google account on device
                    new AlertDialog.Builder(OnboardingActivity.this)
                            .setTitle("No Google Account")
                            .setMessage("You don’t have a Google account set up on this device. " +
                                    "Would you like to create one now?")
                            .setPositiveButton("Create account", (dlg, which) -> {
                                // launch system “add account” screen, filtered to Google
                                Intent add = new Intent(Settings.ACTION_ADD_ACCOUNT);
                                add.putExtra(Settings.EXTRA_AUTHORITIES,
                                        new String[] { "com.google" });
                                startActivity(add);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();

                } else {
                    // 3) Anything else → report via email
                    new AlertDialog.Builder(OnboardingActivity.this)
                            .setTitle("Sign-In Error")
                            .setMessage("An unexpected error occurred. Would you like to report it?")
                            .setPositiveButton("Report via email", (dlg, which) -> {
                                Intent email = new Intent(Intent.ACTION_SENDTO,
                                        Uri.parse("mailto:harishtanu007@gmail.com"));
                                email.putExtra(Intent.EXTRA_SUBJECT, "Sign-In Error Report");
                                email.putExtra(Intent.EXTRA_TEXT,
                                        "Error details:\n" + e.getClass().getSimpleName() +
                                                "\n" + e.getMessage());
                                startActivity(Intent.createChooser(email, "Send email…"));
                            })
                            .setNegativeButton("OK", null)
                            .show();
                }
            }
        });
    }

    private void continueAsGuest() {
        showLoading(true);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREF_IS_GUEST, true);
        editor.putInt(UserRepository.KEY_CURRENT_STREAK, 0);
        editor.putInt(UserRepository.KEY_TOTAL_XP, 0);
        editor.putString(UserRepository.KEY_LAST_PLAYED_DATE, "");
        editor.putLong(UserRepository.KEY_LAST_PLAYED_TS, 0L);
        editor.apply();

        onboardingCompleted = true;
        analytics.logOnboardingCompleted();
        launchMainActivity();
    }

    // No onActivityResult needed for CredentialManager flow

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseService.getAuth().signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = firebaseService.getCurrentUser();
                    if (user != null) {
                        userRepository.createOrUpdateUser(
                                user.getUid(),
                                user.getDisplayName(),
                                user.getEmail()
                        ).addOnSuccessListener(v -> launchMainActivity())
                         .addOnFailureListener(err -> {
                             ExceptionLogger.log("OnboardingActivity", err);
                             String msg = "Failed to update profile: " + err.getMessage();
                             Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
                             binding.getRoot().announceForAccessibility(msg);
                             showLoading(false);
                         });
                    } else {
                        String msg = "Authentication succeeded but no user found.";
                        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
                        binding.getRoot().announceForAccessibility(msg);
                        showLoading(false);
                    }
                })
                .addOnFailureListener(e -> {
                    ExceptionLogger.log("OnboardingActivity", e);
                    String msg = "Firebase authentication failed: " + e.getMessage();
                    Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
                    binding.getRoot().announceForAccessibility(msg);
                    showLoading(false);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!onboardingCompleted) {
            analytics.logOnboardingSkipped();
        }
        SoundManager.getInstance(this).release();
    }

    private void launchMainActivity() {
        analytics.logOnboardingCompleted();
        onboardingCompleted = true;
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
    // If the user manually taps "back," we might want to re-ask next time, so do not override onBackPressed here.
}
