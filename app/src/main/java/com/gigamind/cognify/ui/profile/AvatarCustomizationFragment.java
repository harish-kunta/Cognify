package com.gigamind.cognify.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gigamind.cognify.R;
import com.gigamind.cognify.util.Constants;

/**
 * Simple UI allowing the user to select avatar frame, hat and color.
 * Stores the selections in SharedPreferences under PREF_APP.
 */
public class AvatarCustomizationFragment extends Fragment {
    private ImageView avatarPreview;
    private Spinner frameSpinner;
    private Spinner hatSpinner;
    private Spinner colorSpinner;
    private SharedPreferences prefs;

    private static final String KEY_AVATAR_FRAME = "avatar_frame";
    private static final String KEY_AVATAR_HAT = "avatar_hat";
    private static final String KEY_AVATAR_COLOR = "avatar_color";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_avatar_customization, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireContext().getSharedPreferences(Constants.PREF_APP, Context.MODE_PRIVATE);

        avatarPreview = view.findViewById(R.id.avatarPreview);
        frameSpinner = view.findViewById(R.id.frameSpinner);
        hatSpinner = view.findViewById(R.id.hatSpinner);
        colorSpinner = view.findViewById(R.id.colorSpinner);
        Button saveButton = view.findViewById(R.id.saveAvatarButton);

        setupSpinners();
        loadSelections();

        // Update preview whenever an option is changed
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                updatePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        frameSpinner.setOnItemSelectedListener(listener);
        hatSpinner.setOnItemSelectedListener(listener);
        colorSpinner.setOnItemSelectedListener(listener);

        saveButton.setOnClickListener(v -> {
            prefs.edit()
                    .putString(KEY_AVATAR_FRAME, frameSpinner.getSelectedItem().toString())
                    .putString(KEY_AVATAR_HAT, hatSpinner.getSelectedItem().toString())
                    .putString(KEY_AVATAR_COLOR, colorSpinner.getSelectedItem().toString())
                    .apply();
            requireActivity().onBackPressed();
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> frameAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Circle", "Square"});
        frameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frameSpinner.setAdapter(frameAdapter);

        ArrayAdapter<String> hatAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"None", "Crown", "Cap"});
        hatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hatSpinner.setAdapter(hatAdapter);

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Blue", "Green", "Red"});
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);
    }

    private void loadSelections() {
        String frame = prefs.getString(KEY_AVATAR_FRAME, "Circle");
        String hat = prefs.getString(KEY_AVATAR_HAT, "None");
        String color = prefs.getString(KEY_AVATAR_COLOR, "Blue");

        frameSpinner.setSelection(frame.equals("Square") ? 1 : 0);
        hatSpinner.setSelection(hat.equals("Crown") ? 1 : hat.equals("Cap") ? 2 : 0);
        colorSpinner.setSelection(color.equals("Green") ? 1 : color.equals("Red") ? 2 : 0);
        updatePreview();
    }

    private void updatePreview() {
        String frame = frameSpinner.getSelectedItem().toString();
        String color = colorSpinner.getSelectedItem().toString();

        if (frame.equals("Square")) {
            avatarPreview.setBackgroundResource(R.drawable.avatar_frame_square);
        } else {
            avatarPreview.setBackgroundResource(R.drawable.avatar_frame_circle);
        }

        if (color.equals("Green")) {
            avatarPreview.setColorFilter(getResources().getColor(R.color.success));
        } else if (color.equals("Red")) {
            avatarPreview.setColorFilter(getResources().getColor(R.color.error));
        } else {
            avatarPreview.setColorFilter(getResources().getColor(R.color.blue));
        }
    }
}
