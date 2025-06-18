package com.gigamind.cognify.ui.avatar;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gigamind.cognify.ui.BaseActivity;

import com.gigamind.cognify.R;
import com.gigamind.cognify.adapter.AvatarOptionAdapter;
import com.gigamind.cognify.adapter.AvatarOptionAdapter.AvatarOption;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.util.Constants;

import java.util.Arrays;
import java.util.List;

public class AvatarMakerActivity extends BaseActivity {
    private ImageView hairView;
    private ImageView eyesView;
    private ImageView mouthView;
    private ImageView skinView;
    private ImageView accessoriesView;
    private ImageView clothesView;
    private ImageView eyebrowsView;
    private ImageView facialHairView;
    private ImageView glassesView;
    private ImageView tattooView;
    private FrameLayout avatarContainer;
    private Button saveAvatarButton;
    private UserRepository userRepository;

    private int findOptionIndex(List<AvatarOption> options, int resId) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).applyResId == resId) {
                return i;
            }
        }
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_maker);

        hairView = findViewById(R.id.hairView);
        eyesView = findViewById(R.id.eyesView);
        mouthView = findViewById(R.id.mouthView);
        skinView = findViewById(R.id.skinView);
        accessoriesView = findViewById(R.id.accessoriesView);
        clothesView = findViewById(R.id.clothesView);
        eyebrowsView = findViewById(R.id.eyebrowsView);
        facialHairView = findViewById(R.id.facialHairView);
        glassesView = findViewById(R.id.glassesView);
        tattooView = findViewById(R.id.tattooView);
        avatarContainer = findViewById(R.id.avatarContainer);
        saveAvatarButton = findViewById(R.id.saveAvatarButton);
        userRepository = new UserRepository(this);

        int savedHair = userRepository.getAvatarOption(Constants.AVATAR_HAIR);
        if (savedHair != 0) hairView.setImageResource(savedHair);
        int savedEyes = userRepository.getAvatarOption(Constants.AVATAR_EYES);
        if (savedEyes != 0) eyesView.setImageResource(savedEyes);
        int savedMouth = userRepository.getAvatarOption(Constants.AVATAR_MOUTH);
        if (savedMouth != 0) mouthView.setImageResource(savedMouth);
        int savedSkin = userRepository.getAvatarOption(Constants.AVATAR_SKIN);
        if (savedSkin != 0) skinView.setImageResource(savedSkin);
        int savedAccessory = userRepository.getAvatarOption(Constants.AVATAR_ACCESSORY);
        if (savedAccessory != 0) accessoriesView.setImageResource(savedAccessory);
        int savedClothes = userRepository.getAvatarOption(Constants.AVATAR_BODY);
        if (savedClothes != 0) clothesView.setImageResource(savedClothes);
        int savedBrows = userRepository.getAvatarOption(Constants.AVATAR_EYEBROWS);
        if (savedBrows != 0) eyebrowsView.setImageResource(savedBrows);
        int savedFacial = userRepository.getAvatarOption(Constants.AVATAR_FACIAL_HAIR);
        if (savedFacial != 0) facialHairView.setImageResource(savedFacial);
        int savedGlasses = userRepository.getAvatarOption(Constants.AVATAR_GLASSES);
        if (savedGlasses != 0) glassesView.setImageResource(savedGlasses);
        int savedTattoo = userRepository.getAvatarOption(Constants.AVATAR_TATTOO);
        if (savedTattoo != 0) tattooView.setImageResource(savedTattoo);

        RecyclerView hairRecycler = findViewById(R.id.hairRecyclerView);
        RecyclerView eyesRecycler = findViewById(R.id.eyesRecyclerView);
        RecyclerView mouthRecycler = findViewById(R.id.mouthRecyclerView);
        RecyclerView skinRecycler = findViewById(R.id.skinRecyclerView);
        RecyclerView accessoriesRecycler = findViewById(R.id.accessoriesRecyclerView);
        RecyclerView clothesRecycler = findViewById(R.id.clothesRecyclerView);
        RecyclerView eyebrowsRecycler = findViewById(R.id.eyebrowsRecyclerView);
        RecyclerView facialHairRecycler = findViewById(R.id.facialHairRecyclerView);
        RecyclerView glassesRecycler = findViewById(R.id.glassesRecyclerView);
        RecyclerView tattooRecycler = findViewById(R.id.tattooRecyclerView);

        List<AvatarOption> hairOptions = Arrays.asList(
                new AvatarOption(R.drawable.hair_long, R.drawable.hair_long),
                new AvatarOption(R.drawable.hair_bun, R.drawable.hair_bun),
                new AvatarOption(R.drawable.hair_curly, R.drawable.hair_curly),
                new AvatarOption(R.drawable.hair_longbob, R.drawable.hair_longbob),
                new AvatarOption(R.drawable.hair_nottoolong, R.drawable.hair_nottoolong),
                new AvatarOption(R.drawable.hair_longhairstraight, R.drawable.hair_longhairstraight),
                new AvatarOption(R.drawable.hair_longhairstraight2, R.drawable.hair_longhairstraight2),
                new AvatarOption(R.drawable.hair_longhaircurvy, R.drawable.hair_longhaircurvy)
        );

        int hairIndex = findOptionIndex(hairOptions, savedHair);
        AvatarOptionAdapter hairAdapter = new AvatarOptionAdapter(hairOptions, resId -> {
                hairView.setImageResource(resId);
                userRepository.saveAvatarOption(Constants.AVATAR_HAIR, resId);
        }, hairIndex);
        hairRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hairRecycler.setAdapter(hairAdapter);

        List<AvatarOption> eyesOptions = Arrays.asList(
                new AvatarOption(R.drawable.eyes_default, R.drawable.eyes_default),
                new AvatarOption(R.drawable.eyes_happy, R.drawable.eyes_happy),
                new AvatarOption(R.drawable.eyes_hearts, R.drawable.eyes_hearts),
                new AvatarOption(R.drawable.eyes_dizzy, R.drawable.eyes_dizzy),
                new AvatarOption(R.drawable.eyes_wink, R.drawable.eyes_wink)
        );
        int eyesIndex = findOptionIndex(eyesOptions, savedEyes);
        AvatarOptionAdapter eyesAdapter = new AvatarOptionAdapter(eyesOptions, resId -> {
                eyesView.setImageResource(resId);
                userRepository.saveAvatarOption(Constants.AVATAR_EYES, resId);
        }, eyesIndex);
        eyesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        eyesRecycler.setAdapter(eyesAdapter);

        List<AvatarOption> mouthOptions = Arrays.asList(
                new AvatarOption(R.drawable.mouth_smile, R.drawable.mouth_smile),
                new AvatarOption(R.drawable.mouth_twinkle, R.drawable.mouth_twinkle),
                new AvatarOption(R.drawable.mouth_tongue, R.drawable.mouth_tongue),
                new AvatarOption(R.drawable.mouth_serious, R.drawable.mouth_serious)
        );
        int mouthIndex = findOptionIndex(mouthOptions, savedMouth);
        AvatarOptionAdapter mouthAdapter = new AvatarOptionAdapter(mouthOptions, resId -> {
                mouthView.setImageResource(resId);
                userRepository.saveAvatarOption(Constants.AVATAR_MOUTH, resId);
        }, mouthIndex);
        mouthRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mouthRecycler.setAdapter(mouthAdapter);

        List<AvatarOption> accessoriesOptions = Arrays.asList(
                new AvatarOption(R.drawable.ic_none, 0),
                new AvatarOption(R.drawable.acc_earphones, R.drawable.acc_earphones),
                new AvatarOption(R.drawable.acc_earring1, R.drawable.acc_earring1)
        );
        int accIndex = findOptionIndex(accessoriesOptions, savedAccessory);
        AvatarOptionAdapter accessoriesAdapter = new AvatarOptionAdapter(accessoriesOptions, resId -> {
                accessoriesView.setImageResource(resId);
                userRepository.saveAvatarOption(Constants.AVATAR_ACCESSORY, resId);
        }, accIndex);
        accessoriesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        accessoriesRecycler.setAdapter(accessoriesAdapter);

        List<AvatarOption> clothesOptions = Arrays.asList(
                new AvatarOption(R.drawable.clothes_blazer, R.drawable.clothes_blazer),
                new AvatarOption(R.drawable.clothes_overall, R.drawable.clothes_overall)
        );
        int clothesIndex = findOptionIndex(clothesOptions, savedClothes);
        AvatarOptionAdapter clothesAdapter = new AvatarOptionAdapter(clothesOptions, resId -> {
                clothesView.setImageResource(resId);
                userRepository.saveAvatarOption(Constants.AVATAR_BODY, resId);
        }, clothesIndex);
        clothesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        clothesRecycler.setAdapter(clothesAdapter);

        List<AvatarOption> eyebrowsOptions = Arrays.asList(
                new AvatarOption(R.drawable.eyebrows_default, R.drawable.eyebrows_default),
                new AvatarOption(R.drawable.eyebrows_angry, R.drawable.eyebrows_angry)
        );
        int browsIndex = findOptionIndex(eyebrowsOptions, savedBrows);
        AvatarOptionAdapter eyebrowsAdapter = new AvatarOptionAdapter(eyebrowsOptions, resId -> {
                eyebrowsView.setImageResource(resId);
                userRepository.saveAvatarOption(Constants.AVATAR_EYEBROWS, resId);
        }, browsIndex);
        eyebrowsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        eyebrowsRecycler.setAdapter(eyebrowsAdapter);

        List<AvatarOption> facialHairOptions = Arrays.asList(
                new AvatarOption(R.drawable.ic_none, 0),
                new AvatarOption(R.drawable.facialhair_magnum, R.drawable.facialhair_magnum),
                new AvatarOption(R.drawable.facialhair_fancy, R.drawable.facialhair_fancy)
        );
        int facialIndex = findOptionIndex(facialHairOptions, savedFacial);
        AvatarOptionAdapter facialHairAdapter = new AvatarOptionAdapter(facialHairOptions, resId -> {
                facialHairView.setImageResource(resId);
                userRepository.saveAvatarOption(Constants.AVATAR_FACIAL_HAIR, resId);
        }, facialIndex);
        facialHairRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        facialHairRecycler.setAdapter(facialHairAdapter);

        List<AvatarOption> glassesOptions = Arrays.asList(
                new AvatarOption(R.drawable.ic_none, 0),
                new AvatarOption(R.drawable.glasses_rambo, R.drawable.glasses_rambo),
                new AvatarOption(R.drawable.glasses_nerd, R.drawable.glasses_nerd)
        );
        int glassesIndex = findOptionIndex(glassesOptions, savedGlasses);
        AvatarOptionAdapter glassesAdapter = new AvatarOptionAdapter(glassesOptions, resId -> {
                glassesView.setImageResource(resId);
                userRepository.saveAvatarOption(Constants.AVATAR_GLASSES, resId);
        }, glassesIndex);
        glassesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        glassesRecycler.setAdapter(glassesAdapter);

        List<AvatarOption> tattooOptions = Arrays.asList(
                new AvatarOption(R.drawable.ic_none, 0),
                new AvatarOption(R.drawable.tattoo_harry, R.drawable.tattoo_harry),
                new AvatarOption(R.drawable.tattoo_tribal, R.drawable.tattoo_tribal)
        );
        int tattooIndex = findOptionIndex(tattooOptions, savedTattoo);
        AvatarOptionAdapter tattooAdapter = new AvatarOptionAdapter(tattooOptions, resId -> {
                tattooView.setImageResource(resId);
                userRepository.saveAvatarOption(Constants.AVATAR_TATTOO, resId);
        }, tattooIndex);
        tattooRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tattooRecycler.setAdapter(tattooAdapter);

        List<AvatarOption> skinOptions = Arrays.asList(
                new AvatarOption(R.drawable.skin_white, R.drawable.skin_white),
                new AvatarOption(R.drawable.skin_black, R.drawable.skin_black)
        );
        int skinIndex = findOptionIndex(skinOptions, savedSkin);
        AvatarOptionAdapter skinAdapter = new AvatarOptionAdapter(skinOptions, resId -> {
                skinView.setImageResource(resId);
                userRepository.saveAvatarOption(Constants.AVATAR_SKIN, resId);
        }, skinIndex);
        skinRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        skinRecycler.setAdapter(skinAdapter);

        saveAvatarButton.setOnClickListener(v -> {
            Bitmap bitmap = Bitmap.createBitmap(avatarContainer.getWidth(), avatarContainer.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            avatarContainer.draw(canvas);

            userRepository.saveProfilePicture(bitmap)
                    .addOnSuccessListener(task -> finish());
        });
    }
}
