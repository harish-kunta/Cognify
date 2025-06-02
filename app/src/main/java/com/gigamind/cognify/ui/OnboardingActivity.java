package com.gigamind.cognify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.gigamind.cognify.R;
import com.gigamind.cognify.databinding.ActivityOnboardingBinding;
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

public class OnboardingActivity extends AppCompatActivity {
    private ActivityOnboardingBinding binding;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private static final int RC_SIGN_IN = 9001;

    private static final String COLLECTION_USERS = "users";

    private static class OnboardingItem {
        int imageResId;
        String title;
        String description;

        OnboardingItem(int imageResId, String title, String description) {
            this.imageResId = imageResId;
            this.title = title;
            this.description = description;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Configure Google Sign In
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
        items.add(new OnboardingItem(R.drawable.brain_train, "Train your brain in 60 seconds a day", "Quick, fun exercises to keep your mind sharp"));
        items.add(new OnboardingItem(R.drawable.word_math, "Form words. Solve math. Beat the clock!", "Challenge yourself with various brain games"));
        items.add(new OnboardingItem(R.drawable.rewards, "Earn streaks, top leaderboards!", "Compete and track your progress"));
        items.add(new OnboardingItem(R.drawable.profile, "Sign in to save progress", "Or continue as guest to try it out"));

        OnboardingAdapter adapter = new OnboardingAdapter(items);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    // Optional: customize tab here
                }).attach();

        binding.tabLayout.post(() -> {
            ViewGroup tabStrip = (ViewGroup) binding.tabLayout.getChildAt(0);
            for (int i = 0; i < tabStrip.getChildCount(); i++) {
                View tabView = tabStrip.getChildAt(i);
                ViewGroup.LayoutParams lp = tabView.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT; // 🔑 KEY FIX
                tabView.setLayoutParams(lp);

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tabView.getLayoutParams();
                params.setMargins(12, 0, 12, 0); // spacing between dots
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
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    // Exchange ID token for Firebase credential
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
                    // User is now signed in to Firebase
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // Write or merge their details into Firestore
                        createOrUpdateUserInFirestore(user, gAccount);
                    } else {
                        // Unlikely, but if user is null:
                        Toast.makeText(this, "Authentication succeeded but no user found.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Firebase authentication failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void createOrUpdateUserInFirestore(FirebaseUser user, GoogleSignInAccount gAccount) {
        String uid   = user.getUid();
        String name  = gAccount.getDisplayName();
        String email = gAccount.getEmail();

        // Build the map of initial (or existing) fields
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("name", name != null ? name : "");
        userData.put("email", email != null ? email : "");
        // If you want to preserve existing scores/streaks if user returns:
        // you could call get() first, but for simplicity we set defaults on first sign-in:
        userData.put("score", 0);
        userData.put("currentStreak", 0);
        userData.put("leaderboardRank", 0);
        userData.put("trophies", new ArrayList<>()); // empty list

        DocumentReference userRef = firestore
                .collection(COLLECTION_USERS)
                .document(uid);

        // Use set(..., SetOptions.merge()) so we don't overwrite existing fields if they already exist
        userRef.set(userData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Firestore write succeeded → go to MainActivity
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

    private static class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {
        private final List<OnboardingItem> items;

        OnboardingAdapter(List<OnboardingItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding, parent, false);
            return new OnboardingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
            OnboardingItem item = items.get(position);
            holder.imageView.setImageResource(item.imageResId);
            holder.titleText.setText(item.title);
            holder.descriptionText.setText(item.description);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class OnboardingViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView titleText;
            TextView descriptionText;

            OnboardingViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                titleText = itemView.findViewById(R.id.titleText);
                descriptionText = itemView.findViewById(R.id.descriptionText);
            }
        }
    }
} 