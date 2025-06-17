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
    private ImageView bodyView;
    private ImageView noseView;
    private ImageView earsView;
    private ImageView facialHairView;
    private ImageView accessoryView;

    private RecyclerView faceShapeRecycler;
    private RecyclerView skinRecycler;
    private RecyclerView hairRecycler;
    private RecyclerView eyesRecycler;
    private RecyclerView mouthRecycler;
    private RecyclerView bodyRecycler;
    private RecyclerView noseRecycler;
    private RecyclerView earsRecycler;
    private RecyclerView facialHairRecycler;
    private RecyclerView accessoryRecycler;

    private AvatarOptionAdapter skinAdapter;
    private AvatarOptionAdapter hairAdapter;
    private AvatarOptionAdapter eyesAdapter;
    private AvatarOptionAdapter mouthAdapter;
    private AvatarOptionAdapter bodyAdapter;
    private AvatarOptionAdapter noseAdapter;
    private AvatarOptionAdapter earsAdapter;
    private AvatarOptionAdapter facialHairAdapter;
    private AvatarOptionAdapter accessoryAdapter;
    private AvatarOptionAdapter faceShapeAdapter;
    private SharedPreferences prefs;

    private static final String KEY_SKIN = Constants.AVATAR_SKIN;
    private static final String KEY_HAIR = Constants.AVATAR_HAIR;
    private static final String KEY_EYES = Constants.AVATAR_EYES;
    private static final String KEY_MOUTH = Constants.AVATAR_MOUTH;
    private static final String KEY_BODY = Constants.AVATAR_BODY;
    private static final String KEY_NOSE = Constants.AVATAR_NOSE;
    private static final String KEY_EARS = Constants.AVATAR_EARS;
    private static final String KEY_FACIAL_HAIR = Constants.AVATAR_FACIAL_HAIR;
    private static final String KEY_ACCESSORY = Constants.AVATAR_ACCESSORY;
    private static final String KEY_FACE_SHAPE = Constants.AVATAR_FACE_SHAPE;

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
        bodyView = view.findViewById(R.id.bodyView);
        noseView = view.findViewById(R.id.noseView);
        earsView = view.findViewById(R.id.earsView);
        facialHairView = view.findViewById(R.id.facialHairView);
        accessoryView = view.findViewById(R.id.accessoryView);
        faceShapeRecycler = view.findViewById(R.id.faceShapeRecycler);

        skinRecycler = view.findViewById(R.id.skinRecycler);
        hairRecycler = view.findViewById(R.id.hairRecycler);
        eyesRecycler = view.findViewById(R.id.eyesRecycler);
        mouthRecycler = view.findViewById(R.id.mouthRecycler);
        bodyRecycler = view.findViewById(R.id.bodyRecycler);
        noseRecycler = view.findViewById(R.id.noseRecycler);
        earsRecycler = view.findViewById(R.id.earsRecycler);
        facialHairRecycler = view.findViewById(R.id.facialHairRecycler);
        accessoryRecycler = view.findViewById(R.id.accessoryRecycler);
        Button saveButton = view.findViewById(R.id.saveAvatarButton);

        setupRecyclers();
        loadSelections();

        saveButton.setOnClickListener(v -> {
            prefs.edit()
                    .putInt(KEY_SKIN, skinAdapter.getSelectedIndex())
                    .putInt(KEY_HAIR, hairAdapter.getSelectedIndex())
                    .putInt(KEY_EYES, eyesAdapter.getSelectedIndex())
                    .putInt(KEY_MOUTH, mouthAdapter.getSelectedIndex())
                    .putInt(KEY_BODY, bodyAdapter.getSelectedIndex())
                    .putInt(KEY_NOSE, noseAdapter.getSelectedIndex())
                    .putInt(KEY_EARS, earsAdapter.getSelectedIndex())
                    .putInt(KEY_FACIAL_HAIR, facialHairAdapter.getSelectedIndex())
                    .putInt(KEY_ACCESSORY, accessoryAdapter.getSelectedIndex())
                    .putInt(KEY_FACE_SHAPE, faceShapeAdapter.getSelectedIndex())
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

        int[] faceShapeOptions = {
                R.drawable.avatar_face,
                R.drawable.avatar_face_square,
                R.drawable.avatar_face_oval
        };
        faceShapeAdapter = new AvatarOptionAdapter(faceShapeOptions, pos -> updatePreview());
        faceShapeRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        faceShapeRecycler.setAdapter(faceShapeAdapter);

        int[] hairOptions = {
                R.drawable.avatar_hair_1,
                R.drawable.avatar_hair_2,
                R.drawable.avatar_hair_3,
                R.drawable.avatar_hair_4,
                R.drawable.avatar_hair_5,
                R.drawable.avatar_hair_6,
                R.drawable.avatar_hair_7
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

        int[] bodyOptions = {
                R.drawable.avatar_body_1,
                R.drawable.avatar_body_2,
                R.drawable.avatar_body_3
        };
        bodyAdapter = new AvatarOptionAdapter(bodyOptions, pos -> updatePreview());
        bodyRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        bodyRecycler.setAdapter(bodyAdapter);

        int[] noseOptions = {
                R.drawable.avatar_nose_1,
                R.drawable.avatar_nose_2,
                R.drawable.avatar_nose_3
        };
        noseAdapter = new AvatarOptionAdapter(noseOptions, pos -> updatePreview());
        noseRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        noseRecycler.setAdapter(noseAdapter);

        int[] earsOptions = {
                R.drawable.avatar_ears_1,
                R.drawable.avatar_ears_2,
                R.drawable.avatar_ears_3
        };
        earsAdapter = new AvatarOptionAdapter(earsOptions, pos -> updatePreview());
        earsRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        earsRecycler.setAdapter(earsAdapter);

        int[] facialHairOptions = {
                R.drawable.avatar_facial_hair_1,
                R.drawable.avatar_facial_hair_2,
                R.drawable.avatar_facial_hair_3
        };
        facialHairAdapter = new AvatarOptionAdapter(facialHairOptions, pos -> updatePreview());
        facialHairRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        facialHairRecycler.setAdapter(facialHairAdapter);

        int[] accessoryOptions = {
                R.drawable.avatar_accessory_1,
                R.drawable.avatar_accessory_2,
                R.drawable.avatar_accessory_3
        };
        accessoryAdapter = new AvatarOptionAdapter(accessoryOptions, pos -> updatePreview());
        accessoryRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        accessoryRecycler.setAdapter(accessoryAdapter);
    }

    private void loadSelections() {
        int skin = prefs.getInt(KEY_SKIN, 0);
        int hair = prefs.getInt(KEY_HAIR, 0);
        int eyes = prefs.getInt(KEY_EYES, 0);
        int mouth = prefs.getInt(KEY_MOUTH, 0);
        int body = prefs.getInt(KEY_BODY, 0);
        int nose = prefs.getInt(KEY_NOSE, 0);
        int ears = prefs.getInt(KEY_EARS, 0);
        int facial = prefs.getInt(KEY_FACIAL_HAIR, 0);
        int accessory = prefs.getInt(KEY_ACCESSORY, 0);
        int faceShape = prefs.getInt(KEY_FACE_SHAPE, 0);

        skinAdapter.setSelectedIndex(skin);
        hairAdapter.setSelectedIndex(hair);
        eyesAdapter.setSelectedIndex(eyes);
        mouthAdapter.setSelectedIndex(mouth);
        bodyAdapter.setSelectedIndex(body);
        noseAdapter.setSelectedIndex(nose);
        earsAdapter.setSelectedIndex(ears);
        facialHairAdapter.setSelectedIndex(facial);
        accessoryAdapter.setSelectedIndex(accessory);
        faceShapeAdapter.setSelectedIndex(faceShape);
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

        int faceShapePos = faceShapeAdapter != null ? faceShapeAdapter.getSelectedIndex() : 0;
        switch (faceShapePos) {
            case 0:
                faceView.setImageResource(R.drawable.avatar_face);
                break;
            case 1:
                faceView.setImageResource(R.drawable.avatar_face_square);
                break;
            case 2:
                faceView.setImageResource(R.drawable.avatar_face_oval);
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
            case 6:
                hairView.setImageResource(R.drawable.avatar_hair_7);
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

        int bodyPos = bodyAdapter != null ? bodyAdapter.getSelectedIndex() : 0;
        switch (bodyPos) {
            case 0:
                bodyView.setImageResource(R.drawable.avatar_body_1);
                break;
            case 1:
                bodyView.setImageResource(R.drawable.avatar_body_2);
                break;
            case 2:
                bodyView.setImageResource(R.drawable.avatar_body_3);
                break;
        }

        int nosePos = noseAdapter != null ? noseAdapter.getSelectedIndex() : 0;
        switch (nosePos) {
            case 0:
                noseView.setImageResource(R.drawable.avatar_nose_1);
                break;
            case 1:
                noseView.setImageResource(R.drawable.avatar_nose_2);
                break;
            case 2:
                noseView.setImageResource(R.drawable.avatar_nose_3);
                break;
        }

        int earsPos = earsAdapter != null ? earsAdapter.getSelectedIndex() : 0;
        switch (earsPos) {
            case 0:
                earsView.setImageResource(R.drawable.avatar_ears_1);
                break;
            case 1:
                earsView.setImageResource(R.drawable.avatar_ears_2);
                break;
            case 2:
                earsView.setImageResource(R.drawable.avatar_ears_3);
                break;
        }

        int facialPos = facialHairAdapter != null ? facialHairAdapter.getSelectedIndex() : 0;
        switch (facialPos) {
            case 0:
                facialHairView.setImageResource(R.drawable.avatar_facial_hair_1);
                break;
            case 1:
                facialHairView.setImageResource(R.drawable.avatar_facial_hair_2);
                break;
            case 2:
                facialHairView.setImageResource(R.drawable.avatar_facial_hair_3);
                break;
        }

        int accessoryPos = accessoryAdapter != null ? accessoryAdapter.getSelectedIndex() : 0;
        switch (accessoryPos) {
            case 0:
                accessoryView.setImageResource(R.drawable.avatar_accessory_1);
                break;
            case 1:
                accessoryView.setImageResource(R.drawable.avatar_accessory_2);
                break;
            case 2:
                accessoryView.setImageResource(R.drawable.avatar_accessory_3);
                break;
        }
    }
}
