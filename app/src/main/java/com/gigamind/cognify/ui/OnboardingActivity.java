package com.gigamind.cognify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
 * OnboardingActivity now uses UserFields constants for all Firestore field names,
 * so that we never hard-code key names in multiple places.
 */
public class OnboardingActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private ActivityOnboardingBinding binding;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Configure Google Sign In (web client ID stored in strings.xml)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        setupOnboarding();
        setupButtons();
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
        binding.btnSignIn.setOnClickListener(v -> signIn());
        binding.btnContinueAsGuest.setOnClickListener(v -> continueAsGuest());
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
     * Writes or merges the newly signed‐in user’s data into Firestore.
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
}
