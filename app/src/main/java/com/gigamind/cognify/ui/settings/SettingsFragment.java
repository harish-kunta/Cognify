package com.gigamind.cognify.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;

import com.gigamind.cognify.R;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.databinding.FragmentSettingsBinding;
import com.gigamind.cognify.ui.OnboardingActivity;
import com.gigamind.cognify.util.Constants;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.util.SoundManager;
import com.gigamind.cognify.util.GoogleSignInHelper;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.gigamind.cognify.util.ExceptionLogger;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private SharedPreferences prefs;
    private static final String KEY_SOUND_ENABLED = Constants.PREF_SOUND_ENABLED;
    private static final String KEY_HAPTICS_ENABLED = Constants.PREF_HAPTICS_ENABLED;
    private static final String KEY_ANIMATIONS_ENABLED = Constants.PREF_ANIMATIONS_ENABLED;
    private static final String KEY_DARK_MODE_ENABLED = Constants.PREF_DARK_MODE_ENABLED;
    private static final String KEY_ONBOARDING_COMPLETED = Constants.PREF_ONBOARDING_COMPLETED;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences(Constants.PREF_APP, 0);
        com.gigamind.cognify.animation.AnimatorProvider.updateFromPreferences(requireContext());
        setupPreferences();
        setupButtons();
    }

    private void setupPreferences() {
        // Load saved preferences
        binding.soundEffectsSwitch.setChecked(prefs.getBoolean(KEY_SOUND_ENABLED, true));
        binding.hapticsSwitch.setChecked(prefs.getBoolean(KEY_HAPTICS_ENABLED, true));
        binding.animationsSwitch.setChecked(prefs.getBoolean(KEY_ANIMATIONS_ENABLED, true));
        binding.darkModeSwitch.setChecked(prefs.getBoolean(KEY_DARK_MODE_ENABLED, false));

        if (binding.darkModeSwitch.isChecked()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Set up listeners
        binding.soundEffectsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(KEY_SOUND_ENABLED, isChecked).apply();
                SoundManager.getInstance(requireContext()).playToggle();
        });

        binding.hapticsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, isChecked).apply();
                SoundManager.getInstance(requireContext()).playToggle();
        });

        binding.animationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean(KEY_ANIMATIONS_ENABLED, isChecked).apply();
                com.gigamind.cognify.animation.AnimatorProvider.setAnimationsEnabled(isChecked);
                SoundManager.getInstance(requireContext()).playToggle();
        });

        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            SoundManager.getInstance(requireContext()).playToggle();
        });
    }

    private void setupButtons() {
        binding.replayTutorialButton.setOnClickListener(v -> {
            SoundManager.getInstance(requireContext()).playButton();
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.replay_tutorial)
                    .setMessage(R.string.replay_tutorial_confirm)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, false).apply();
                        startActivity(new Intent(requireContext(), OnboardingActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        binding.resetProgressButton.setOnClickListener(v -> {
            SoundManager.getInstance(requireContext()).playButton();
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.reset_progress)
                    .setMessage(R.string.reset_progress_confirm)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        // Clear all preferences except tutorial completion
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.clear();
                        editor.putBoolean(KEY_ONBOARDING_COMPLETED, true);
                        editor.apply();
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        binding.deleteAccountButton.setOnClickListener(v -> {
            SoundManager.getInstance(requireContext()).playButton();
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_account)
                    .setMessage(R.string.delete_account_confirm)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.yes, (dialog, which) -> startDeleteFlow())
                    .show();
        });

        // Show sign in or sign out button based on auth state
        FirebaseService service = FirebaseService.getInstance();
        binding.deleteAccountButton.setVisibility(service.isUserSignedIn() ? View.VISIBLE : View.GONE);
        if (service.isUserSignedIn()) {
            binding.btnSignIn.setText(R.string.sign_out);
            binding.btnSignIn.setOnClickListener(v -> {
                SoundManager.getInstance(requireContext()).playButton();
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.logout_confirm_title)
                        .setMessage(R.string.logout_confirm_message)
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                            // Just dismiss
                            dialog.dismiss();
                        })
                        .setPositiveButton(R.string.logout_confirm_positive, (dialog, which) -> {
                            // Clear only streak/xp/personal‐best from prefs:
                            SharedPreferences prefs = requireActivity()
                                    .getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
                            prefs.edit()
                                    .remove(UserRepository.KEY_LAST_PLAYED_DATE)
                                    .remove(UserRepository.KEY_LAST_PLAYED_TS)
                                    .remove(UserRepository.KEY_CURRENT_STREAK)
                                    .remove(UserRepository.KEY_TOTAL_XP)
                                    .remove(UserRepository.KEY_PERSONAL_BEST_XP)
                                    .apply();

                            FirebaseService.getInstance().signOut();
                            Intent i = new Intent(requireActivity(), OnboardingActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        })
                        .show();
            });
        } else {
            binding.btnSignIn.setText(R.string.sign_in);
            binding.btnSignIn.setOnClickListener(v -> {
                SoundManager.getInstance(requireContext()).playButton();
                // Start sign in flow
                startActivity(new Intent(requireContext(), OnboardingActivity.class));
            });
        }
    }

    private void startDeleteFlow() {
        GoogleSignInHelper.signIn(requireActivity(), true, new GoogleSignInHelper.Callback() {
            @Override
            public void onSuccess(String idToken) {
                reauthenticateAndDelete(idToken);
            }

            @Override
            public void onError(Exception e) {
                String msg = getString(R.string.google_sign_in_failed);
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
                binding.getRoot().announceForAccessibility(msg);
            }
        });
    }

    private void reauthenticateAndDelete(String idToken) {
        FirebaseUser user = FirebaseService.getInstance().getAuth().getCurrentUser();
        if (user == null) return;

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> FirebaseService.getInstance().deleteAccountAndData()
                        .addOnSuccessListener(v -> {
                            String msg = getString(R.string.account_deleted);
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                            Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
                            binding.getRoot().announceForAccessibility(msg);
                            startActivity(new Intent(requireActivity(), OnboardingActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        })
                        .addOnFailureListener(e -> {
                            ExceptionLogger.log("SettingsFragment", e);
                            String msg = getString(R.string.delete_account_error, e.getMessage());
                            Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
                            binding.getRoot().announceForAccessibility(msg);
                        }))
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        startDeleteFlow();
                    } else {
                        ExceptionLogger.log("SettingsFragment", e);
                        String msg = getString(R.string.delete_account_error, e.getMessage());
                        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
                        binding.getRoot().announceForAccessibility(msg);
                    }
                });
    }

    // No onActivityResult needed for CredentialManager flow (handled by GoogleSignInHelper)

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 