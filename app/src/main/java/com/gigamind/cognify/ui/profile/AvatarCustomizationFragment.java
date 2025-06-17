package com.gigamind.cognify.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gigamind.cognify.adapter.AvatarOptionAdapter;
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

    private RecyclerView skinRecycler;
    private RecyclerView hairRecycler;
    private RecyclerView eyesRecycler;
    private RecyclerView mouthRecycler;

    private AvatarOptionAdapter skinAdapter;
    private AvatarOptionAdapter hairAdapter;
    private AvatarOptionAdapter eyesAdapter;
    private AvatarOptionAdapter mouthAdapter;
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

        skinRecycler = view.findViewById(R.id.skinRecycler);
        hairRecycler = view.findViewById(R.id.hairRecycler);
        eyesRecycler = view.findViewById(R.id.eyesRecycler);
        mouthRecycler = view.findViewById(R.id.mouthRecycler);
        Button saveButton = view.findViewById(R.id.saveAvatarButton);

        setupRecyclers();
        loadSelections();

        saveButton.setOnClickListener(v -> {
            prefs.edit()
                    .putInt(KEY_SKIN, skinAdapter.getSelectedIndex())
                    .putInt(KEY_HAIR, hairAdapter.getSelectedIndex())
                    .putInt(KEY_EYES, eyesAdapter.getSelectedIndex())
                    .putInt(KEY_MOUTH, mouthAdapter.getSelectedIndex())
                    .apply();
            requireActivity().onBackPressed();
        });
    }

    private void setupRecyclers() {
        int[] skinOptions = {
                R.drawable.skin_color_0,
                R.drawable.skin_color_1,
                R.drawable.skin_color_2,
                R.drawable.skin_color_3,
                R.drawable.skin_color_4
        };
        skinAdapter = new AvatarOptionAdapter(skinOptions, pos -> updatePreview());
        skinRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        skinRecycler.setAdapter(skinAdapter);

        int[] hairOptions = {
                R.drawable.avatar_hair_1,
                R.drawable.avatar_hair_2,
                R.drawable.avatar_hair_3,
                R.drawable.avatar_hair_4,
                R.drawable.avatar_hair_5,
                R.drawable.avatar_hair_6
        };
        hairAdapter = new AvatarOptionAdapter(hairOptions, pos -> updatePreview());
        hairRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        hairRecycler.setAdapter(hairAdapter);

        int[] eyesOptions = {
                R.drawable.avatar_eyes_1,
                R.drawable.avatar_eyes_2,
                R.drawable.avatar_eyes_3,
                R.drawable.avatar_eyes_4,
                R.drawable.avatar_eyes_5,
                R.drawable.avatar_eyes_6
        };
        eyesAdapter = new AvatarOptionAdapter(eyesOptions, pos -> updatePreview());
        eyesRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        eyesRecycler.setAdapter(eyesAdapter);

        int[] mouthOptions = {
                R.drawable.avatar_mouth_1,
                R.drawable.avatar_mouth_2,
                R.drawable.avatar_mouth_3,
                R.drawable.avatar_mouth_4,
                R.drawable.avatar_mouth_5,
                R.drawable.avatar_mouth_6
        };
        mouthAdapter = new AvatarOptionAdapter(mouthOptions, pos -> updatePreview());
        mouthRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        mouthRecycler.setAdapter(mouthAdapter);
    }

    private void loadSelections() {
        int skin = prefs.getInt(KEY_SKIN, 0);
        int hair = prefs.getInt(KEY_HAIR, 0);
        int eyes = prefs.getInt(KEY_EYES, 0);
        int mouth = prefs.getInt(KEY_MOUTH, 0);

        skinAdapter.setSelectedIndex(skin);
        hairAdapter.setSelectedIndex(hair);
        eyesAdapter.setSelectedIndex(eyes);
        mouthAdapter.setSelectedIndex(mouth);
        updatePreview();
    }

    private void updatePreview() {
        int skinPos = skinAdapter != null ? skinAdapter.getSelectedIndex() : 0;
        switch (skinPos) {
            case 0:
                faceView.setColorFilter(getResources().getColor(R.color.avatar_skin_very_light));
                break;
            case 1:
                faceView.setColorFilter(getResources().getColor(R.color.avatar_skin_light));
                break;
            case 2:
                faceView.setColorFilter(getResources().getColor(R.color.avatar_skin_medium));
                break;
            case 3:
                faceView.setColorFilter(getResources().getColor(R.color.avatar_skin_dark));
                break;
            case 4:
                faceView.setColorFilter(getResources().getColor(R.color.avatar_skin_very_dark));
                break;
        }

        int hairPos = hairAdapter != null ? hairAdapter.getSelectedIndex() : 0;
        switch (hairPos) {
            case 0:
                hairView.setImageResource(R.drawable.avatar_hair_1);
                break;
            case 1:
                hairView.setImageResource(R.drawable.avatar_hair_2);
                break;
            case 2:
                hairView.setImageResource(R.drawable.avatar_hair_3);
                break;
            case 3:
                hairView.setImageResource(R.drawable.avatar_hair_4);
                break;
            case 4:
                hairView.setImageResource(R.drawable.avatar_hair_5);
                break;
            case 5:
                hairView.setImageResource(R.drawable.avatar_hair_6);
                break;
        }

        int eyesPos = eyesAdapter != null ? eyesAdapter.getSelectedIndex() : 0;
        switch (eyesPos) {
            case 0:
                eyesView.setImageResource(R.drawable.avatar_eyes_1);
                break;
            case 1:
                eyesView.setImageResource(R.drawable.avatar_eyes_2);
                break;
            case 2:
                eyesView.setImageResource(R.drawable.avatar_eyes_3);
                break;
            case 3:
                eyesView.setImageResource(R.drawable.avatar_eyes_4);
                break;
            case 4:
                eyesView.setImageResource(R.drawable.avatar_eyes_5);
                break;
            case 5:
                eyesView.setImageResource(R.drawable.avatar_eyes_6);
                break;
        }

        int mouthPos = mouthAdapter != null ? mouthAdapter.getSelectedIndex() : 0;
        switch (mouthPos) {
            case 0:
                mouthView.setImageResource(R.drawable.avatar_mouth_1);
                break;
            case 1:
                mouthView.setImageResource(R.drawable.avatar_mouth_2);
                break;
            case 2:
                mouthView.setImageResource(R.drawable.avatar_mouth_3);
                break;
            case 3:
                mouthView.setImageResource(R.drawable.avatar_mouth_4);
                break;
            case 4:
                mouthView.setImageResource(R.drawable.avatar_mouth_5);
                break;
            case 5:
                mouthView.setImageResource(R.drawable.avatar_mouth_6);
                break;
        }
    }
}
