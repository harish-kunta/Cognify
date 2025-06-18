package com.gigamind.cognify.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.snackbar.Snackbar;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

import com.gigamind.cognify.util.NotificationPermissionHelper;

import com.gigamind.cognify.R;
import com.gigamind.cognify.adapter.OnboardingAdapter;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.databinding.ActivityOnboardingBinding;
import com.gigamind.cognify.util.OnboardingItem;
import android.os.CancellationSignal;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.PasswordCredential;
import androidx.credentials.PublicKeyCredential;
import androidx.credentials.CustomCredential;
import androidx.credentials.exceptions.GetCredentialException;

import com.gigamind.cognify.util.ExceptionLogger;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gigamind.cognify.analytics.GameAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.gigamind.cognify.util.Constants;
import com.gigamind.cognify.util.SoundManager;


import java.util.ArrayList;
import java.util.List;

/**
 * OnboardingActivity now also asks for notification permission during onboarding.
 * We show a rationale dialog until the user either grants the permission
 * (Android 13+) or explicitly taps getString(R.string.no_thanks) to decline.
 */
public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private CredentialManager credentialManager;
    private FirebaseService firebaseService;
    private SharedPreferences prefs;
    private UserRepository userRepository;
    private NotificationPermissionHelper notificationPermissionHelper;
    private GameAnalytics analytics;
    private boolean onboardingCompleted = false;

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

        // Initialize CredentialManager for Google ID sign in
        credentialManager = CredentialManager.create(this);

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
                Intent i = new Intent(OnboardingActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
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
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .setNonce(UUID.randomUUID().toString())
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // CredentialManager supports back to API 21 via Play Services, so no
        // SDK version check is necessary here.
        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                getMainExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        ExceptionLogger.log("OnboardingActivity", e);
                        String msg = getString(R.string.google_sign_in_failed);
                        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
                        binding.getRoot().announceForAccessibility(msg);
                    }
                }
        );
    }

    private void handleSignIn(GetCredentialResponse result) {
        androidx.credentials.Credential credential = result.getCredential();
        if (credential instanceof CustomCredential) {
            CustomCredential cc = (CustomCredential) credential;
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(cc.getType())) {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cc.getData());
                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
            } else {
                ExceptionLogger.log("OnboardingActivity", new Exception("Unexpected credential type"));
            }
        } else if (credential instanceof PasswordCredential || credential instanceof PublicKeyCredential) {
            // Unsupported credential types for this flow
            ExceptionLogger.log("OnboardingActivity", new Exception("Unexpected credential"));
        }
    }

    private void continueAsGuest() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREF_IS_GUEST, true);
        editor.putInt(UserRepository.KEY_CURRENT_STREAK, 0);
        editor.putInt(UserRepository.KEY_TOTAL_XP, 0);
        editor.putString(UserRepository.KEY_LAST_PLAYED_DATE, "");
        editor.putLong(UserRepository.KEY_LAST_PLAYED_TS, 0L);
        editor.apply();

        onboardingCompleted = true;
        analytics.logOnboardingCompleted();
        startActivity(new Intent(this, MainActivity.class));
        finish();
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
                         });
                    } else {
                        String msg = "Authentication succeeded but no user found.";
                        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
                        binding.getRoot().announceForAccessibility(msg);
                    }
                })
                .addOnFailureListener(e -> {
                    ExceptionLogger.log("OnboardingActivity", e);
                    String msg = "Firebase authentication failed: " + e.getMessage();
                    Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
                    binding.getRoot().announceForAccessibility(msg);
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
        finish();
    }
    // If the user manually taps "back," we might want to re-ask next time, so do not override onBackPressed here.
}
