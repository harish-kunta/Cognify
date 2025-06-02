package com.gigamind.cognify.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gigamind.cognify.R;
import com.gigamind.cognify.databinding.FragmentSettingsBinding;
import com.gigamind.cognify.ui.OnboardingActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("AppPrefs", 0);
        setupPreferences();
        setupButtons();
    }

    private void setupPreferences() {
        // Load saved preferences
        binding.soundEffectsSwitch.setChecked(prefs.getBoolean("sound_enabled", true));
        binding.hapticsSwitch.setChecked(prefs.getBoolean("haptics_enabled", true));
        binding.animationsSwitch.setChecked(prefs.getBoolean("animations_enabled", true));

        // Set up listeners
        binding.soundEffectsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("sound_enabled", isChecked).apply());

        binding.hapticsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("haptics_enabled", isChecked).apply());

        binding.animationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("animations_enabled", isChecked).apply());
    }

    private void setupButtons() {
        binding.replayTutorialButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.replay_tutorial)
                    .setMessage(R.string.replay_tutorial_confirm)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        prefs.edit().putBoolean("tutorial_completed", false).apply();
                        startActivity(new Intent(requireContext(), OnboardingActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        binding.resetProgressButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.reset_progress)
                    .setMessage(R.string.reset_progress_confirm)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        // Clear all preferences except tutorial completion
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.clear();
                        editor.putBoolean("tutorial_completed", true);
                        editor.apply();
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        // Show sign in or sign out button based on auth state
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            binding.btnSignIn.setText(R.string.sign_out);
            binding.btnSignIn.setOnClickListener(v -> {
                auth.signOut();
                requireActivity().recreate();
            });
        } else {
            binding.btnSignIn.setText(R.string.sign_in);
            binding.btnSignIn.setOnClickListener(v -> {
                // Start sign in flow
                startActivity(new Intent(requireContext(), OnboardingActivity.class));
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 