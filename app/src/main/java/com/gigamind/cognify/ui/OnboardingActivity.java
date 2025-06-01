package com.gigamind.cognify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    private ActivityOnboardingBinding binding;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private static final int RC_SIGN_IN = 9001;

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
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Handle sign in failure
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, go to main activity
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
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