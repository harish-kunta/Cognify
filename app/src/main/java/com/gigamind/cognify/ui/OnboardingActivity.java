package com.gigamind.cognify.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gigamind.cognify.util.NotificationPermissionHelper;

import com.gigamind.cognify.R;
import com.gigamind.cognify.adapter.OnboardingAdapter;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.databinding.ActivityOnboardingBinding;
import com.gigamind.cognify.util.OnboardingItem;
import com.gigamind.cognify.util.UserFields;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.gigamind.cognify.util.ExceptionLogger;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.AuthCredential;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.gigamind.cognify.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OnboardingActivity now also asks for notification permission during onboarding.
 * We show a rationale dialog until the user either grants the permission
 * (Android 13+) or explicitly taps getString(R.string.no_thanks) to decline.
 */
public class OnboardingActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;

    private ActivityOnboardingBinding binding;
    private GoogleSignInClient googleSignInClient;
    private FirebaseService firebaseService;
    private SharedPreferences prefs;
    private UserRepository userRepository;
    private NotificationPermissionHelper notificationPermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        firebaseService = FirebaseService.getInstance();
        userRepository = new UserRepository(this);

        // Configure Google Sign In (web client ID stored in strings.xml)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

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
                .title("Train your brain in 60 seconds a day")
                .description("Quick, fun exercises to keep your mind sharp")
                .build());
        items.add(new OnboardingItem.Builder()
                .imageResId(R.drawable.word_math)
                .title("Form words. Solve math. Beat the clock!")
                .description("Challenge yourself with various brain games")
                .build());
        items.add(new OnboardingItem.Builder()
                .imageResId(R.drawable.rewards)
                .title("Earn streaks, top leaderboards!")
                .description("Compete and track your progress")
                .build());
        items.add(new OnboardingItem.Builder()
                .imageResId(R.drawable.profile)
                .title("Sign in to save progress")
                .description("Or continue as guest to try it out")
                .build());

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
        FirebaseUser currentUser = firebaseService.getCurrentUser();
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
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREF_IS_GUEST, true);
        editor.putInt(UserRepository.KEY_CURRENT_STREAK, 0);
        editor.putInt(UserRepository.KEY_TOTAL_XP, 0);
        editor.putString(UserRepository.KEY_LAST_PLAYED_DATE, "");
        editor.putLong(UserRepository.KEY_LAST_PLAYED_TS, 0L);
        editor.apply();

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
                ExceptionLogger.log("OnboardingActivity", e);
                Toast.makeText(this,
                        "Google sign-in failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount gAccount) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseService.getAuth().signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = firebaseService.getCurrentUser();
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

    private void createOrUpdateUserInFirestore(FirebaseUser user, GoogleSignInAccount gAccount) {
        String uid = user.getUid();
        String name = gAccount.getDisplayName();
        String email = gAccount.getEmail();

        DocumentReference userRef = firebaseService.getFirestore()
                .collection(FirebaseService.COLLECTION_USERS)
                .document(uid);

        // First, fetch the document once to see if it already exists:
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // Document already exists → do not reset currentStreak or totalXP.
                // We still might want to ensure name/email are up to date:
                Map<String, Object> updates = new HashMap<>();
                updates.put(UserFields.FIELD_NAME, (name != null ? name : ""));
                updates.put(UserFields.FIELD_EMAIL, (email != null ? email : ""));
                userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            // Now pull down the existing streak/XP into SharedPrefs:
                            userRepository.syncUserDataOnce()
                                    .addOnSuccessListener(snap -> {
                                        launchMainActivity();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Even if syncing fails, at least go on to MainActivity:
                                        launchMainActivity();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(
                                    OnboardingActivity.this,
                                    "Failed to update user profile: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });
            } else {
                // First time ever: document does not exist. Initialize everything to zero:
                Map<String, Object> newUserData = new HashMap<>();
                newUserData.put(UserFields.FIELD_UID, uid);
                newUserData.put(UserFields.FIELD_NAME, (name != null ? name : ""));
                newUserData.put(UserFields.FIELD_EMAIL, (email != null ? email : ""));
                newUserData.put(UserFields.FIELD_CURRENT_STREAK, 0);
                newUserData.put(UserFields.FIELD_TOTAL_XP, 0);
                newUserData.put(UserFields.FIELD_LAST_PLAYED_DATE, "");      // or omit if you prefer
                newUserData.put(UserFields.FIELD_LAST_PLAYED_TS, 0L);       // or omit
                newUserData.put(UserFields.FIELD_LEADERBOARD_RANK, 0);
                newUserData.put(UserFields.FIELD_TROPHIES, new ArrayList<>());
                // (Add any other "first-time" defaults here)

                userRef.set(newUserData, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            // Since we just initialized streak=0 and totalXP=0,
                            // we can write those four keys into SharedPrefs immediately:
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt(UserRepository.KEY_CURRENT_STREAK, 0);
                            editor.putInt(UserRepository.KEY_TOTAL_XP, 0);
                            editor.putString(UserRepository.KEY_LAST_PLAYED_DATE, "");
                            editor.putLong(UserRepository.KEY_LAST_PLAYED_TS, 0L);
                            editor.apply();

                            // Now launch MainActivity:
                            launchMainActivity();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(
                                    OnboardingActivity.this,
                                    "Failed to create new user in Firestore: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(
                    OnboardingActivity.this,
                    "Error checking user document: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        });
    }

    private void launchMainActivity() {
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    // If the user manually taps "back," we might want to re-ask next time, so do not override onBackPressed here.
}
