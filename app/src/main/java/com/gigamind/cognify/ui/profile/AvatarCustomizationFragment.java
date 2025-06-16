package com.gigamind.cognify.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gigamind.cognify.R;
import com.gigamind.cognify.util.Constants;

/**
 * Advanced avatar customization allowing selection of skin color,
 * hair style, eyes and mouth. Values are stored in SharedPreferences
 * under PREF_APP.
 */
public class AvatarCustomizationFragment extends Fragment {
    private ImageView faceView;
    private ImageView hairView;
    private ImageView eyesView;
    private ImageView mouthView;

    private Spinner skinSpinner;
    private Spinner hairSpinner;
    private Spinner eyesSpinner;
    private Spinner mouthSpinner;
    private SharedPreferences prefs;

    private static final String KEY_SKIN = Constants.AVATAR_SKIN;
    private static final String KEY_HAIR = Constants.AVATAR_HAIR;
    private static final String KEY_EYES = Constants.AVATAR_EYES;
    private static final String KEY_MOUTH = Constants.AVATAR_MOUTH;

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

        faceView = view.findViewById(R.id.faceView);
        hairView = view.findViewById(R.id.hairView);
        eyesView = view.findViewById(R.id.eyesView);
        mouthView = view.findViewById(R.id.mouthView);

        skinSpinner = view.findViewById(R.id.skinSpinner);
        hairSpinner = view.findViewById(R.id.hairSpinner);
        eyesSpinner = view.findViewById(R.id.eyesSpinner);
        mouthSpinner = view.findViewById(R.id.mouthSpinner);
        Button saveButton = view.findViewById(R.id.saveAvatarButton);

        setupSpinners();
        loadSelections();

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                updatePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };
        skinSpinner.setOnItemSelectedListener(listener);
        hairSpinner.setOnItemSelectedListener(listener);
        eyesSpinner.setOnItemSelectedListener(listener);
        mouthSpinner.setOnItemSelectedListener(listener);

        saveButton.setOnClickListener(v -> {
            prefs.edit()
                    .putInt(KEY_SKIN, skinSpinner.getSelectedItemPosition())
                    .putInt(KEY_HAIR, hairSpinner.getSelectedItemPosition())
                    .putInt(KEY_EYES, eyesSpinner.getSelectedItemPosition())
                    .putInt(KEY_MOUTH, mouthSpinner.getSelectedItemPosition())
                    .apply();
            requireActivity().onBackPressed();
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> skinAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Light", "Medium", "Dark"});
        skinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skinSpinner.setAdapter(skinAdapter);

        ArrayAdapter<String> hairAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Style 1", "Style 2"});
        hairAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hairSpinner.setAdapter(hairAdapter);

        ArrayAdapter<String> eyesAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Eyes 1", "Eyes 2"});
        eyesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eyesSpinner.setAdapter(eyesAdapter);

        ArrayAdapter<String> mouthAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Mouth 1", "Mouth 2"});
        mouthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mouthSpinner.setAdapter(mouthAdapter);
    }

    private void loadSelections() {
        int skin = prefs.getInt(KEY_SKIN, 0);
        int hair = prefs.getInt(KEY_HAIR, 0);
        int eyes = prefs.getInt(KEY_EYES, 0);
        int mouth = prefs.getInt(KEY_MOUTH, 0);

        skinSpinner.setSelection(skin);
        hairSpinner.setSelection(hair);
        eyesSpinner.setSelection(eyes);
        mouthSpinner.setSelection(mouth);
        updatePreview();
    }

    private void updatePreview() {
        switch (skinSpinner.getSelectedItemPosition()) {
            case 0:
                faceView.setColorFilter(getResources().getColor(R.color.avatar_skin_light));
                break;
            case 1:
                faceView.setColorFilter(getResources().getColor(R.color.avatar_skin_medium));
                break;
            case 2:
                faceView.setColorFilter(getResources().getColor(R.color.avatar_skin_dark));
                break;
        }

        hairView.setImageResource(hairSpinner.getSelectedItemPosition() == 0 ?
                R.drawable.avatar_hair_1 : R.drawable.avatar_hair_2);
        eyesView.setImageResource(eyesSpinner.getSelectedItemPosition() == 0 ?
                R.drawable.avatar_eyes_1 : R.drawable.avatar_eyes_2);
        mouthView.setImageResource(mouthSpinner.getSelectedItemPosition() == 0 ?
                R.drawable.avatar_mouth_1 : R.drawable.avatar_mouth_2);
    }
}
