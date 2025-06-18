package com.gigamind.cognify.ui.avatar;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;

import com.gigamind.cognify.R;
import com.gigamind.cognify.adapter.AvatarOptionAdapter;
import com.gigamind.cognify.adapter.AvatarOptionAdapter.AvatarOption;
import com.gigamind.cognify.data.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

public class AvatarMakerActivity extends AppCompatActivity {
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
        saveAvatarButton.setSoundEffectsEnabled(false);
        userRepository = new UserRepository(this);

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

        AvatarOptionAdapter hairAdapter = new AvatarOptionAdapter(hairOptions, resId ->
                hairView.setImageResource(resId));
        hairRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hairRecycler.setAdapter(hairAdapter);

        List<AvatarOption> eyesOptions = Arrays.asList(
                new AvatarOption(R.drawable.eyes_default, R.drawable.eyes_default),
                new AvatarOption(R.drawable.eyes_happy, R.drawable.eyes_happy),
                new AvatarOption(R.drawable.eyes_hearts, R.drawable.eyes_hearts),
                new AvatarOption(R.drawable.eyes_dizzy, R.drawable.eyes_dizzy),
                new AvatarOption(R.drawable.eyes_wink, R.drawable.eyes_wink)
        );
        AvatarOptionAdapter eyesAdapter = new AvatarOptionAdapter(eyesOptions, resId ->
                eyesView.setImageResource(resId));
        eyesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        eyesRecycler.setAdapter(eyesAdapter);

        List<AvatarOption> mouthOptions = Arrays.asList(
                new AvatarOption(R.drawable.mouth_smile, R.drawable.mouth_smile),
                new AvatarOption(R.drawable.mouth_twinkle, R.drawable.mouth_twinkle),
                new AvatarOption(R.drawable.mouth_tongue, R.drawable.mouth_tongue),
                new AvatarOption(R.drawable.mouth_serious, R.drawable.mouth_serious)
        );
        AvatarOptionAdapter mouthAdapter = new AvatarOptionAdapter(mouthOptions, resId ->
                mouthView.setImageResource(resId));
        mouthRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mouthRecycler.setAdapter(mouthAdapter);

        List<AvatarOption> accessoriesOptions = Arrays.asList(
                new AvatarOption(R.drawable.ic_none, 0),
                new AvatarOption(R.drawable.acc_earphones, R.drawable.acc_earphones),
                new AvatarOption(R.drawable.acc_earring1, R.drawable.acc_earring1)
        );
        AvatarOptionAdapter accessoriesAdapter = new AvatarOptionAdapter(accessoriesOptions, resId ->
                accessoriesView.setImageResource(resId));
        accessoriesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        accessoriesRecycler.setAdapter(accessoriesAdapter);

        List<AvatarOption> clothesOptions = Arrays.asList(
                new AvatarOption(R.drawable.clothes_blazer, R.drawable.clothes_blazer),
                new AvatarOption(R.drawable.clothes_overall, R.drawable.clothes_overall)
        );
        AvatarOptionAdapter clothesAdapter = new AvatarOptionAdapter(clothesOptions, resId ->
                clothesView.setImageResource(resId));
        clothesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        clothesRecycler.setAdapter(clothesAdapter);

        List<AvatarOption> eyebrowsOptions = Arrays.asList(
                new AvatarOption(R.drawable.eyebrows_default, R.drawable.eyebrows_default),
                new AvatarOption(R.drawable.eyebrows_angry, R.drawable.eyebrows_angry)
        );
        AvatarOptionAdapter eyebrowsAdapter = new AvatarOptionAdapter(eyebrowsOptions, resId ->
                eyebrowsView.setImageResource(resId));
        eyebrowsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        eyebrowsRecycler.setAdapter(eyebrowsAdapter);

        List<AvatarOption> facialHairOptions = Arrays.asList(
                new AvatarOption(R.drawable.ic_none, 0),
                new AvatarOption(R.drawable.facialhair_magnum, R.drawable.facialhair_magnum),
                new AvatarOption(R.drawable.facialhair_fancy, R.drawable.facialhair_fancy)
        );
        AvatarOptionAdapter facialHairAdapter = new AvatarOptionAdapter(facialHairOptions, resId ->
                facialHairView.setImageResource(resId));
        facialHairRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        facialHairRecycler.setAdapter(facialHairAdapter);

        List<AvatarOption> glassesOptions = Arrays.asList(
                new AvatarOption(R.drawable.ic_none, 0),
                new AvatarOption(R.drawable.glasses_rambo, R.drawable.glasses_rambo),
                new AvatarOption(R.drawable.glasses_nerd, R.drawable.glasses_nerd)
        );
        AvatarOptionAdapter glassesAdapter = new AvatarOptionAdapter(glassesOptions, resId ->
                glassesView.setImageResource(resId));
        glassesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        glassesRecycler.setAdapter(glassesAdapter);

        List<AvatarOption> tattooOptions = Arrays.asList(
                new AvatarOption(R.drawable.ic_none, 0),
                new AvatarOption(R.drawable.tattoo_harry, R.drawable.tattoo_harry),
                new AvatarOption(R.drawable.tattoo_tribal, R.drawable.tattoo_tribal)
        );
        AvatarOptionAdapter tattooAdapter = new AvatarOptionAdapter(tattooOptions, resId ->
                tattooView.setImageResource(resId));
        tattooRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tattooRecycler.setAdapter(tattooAdapter);

        List<AvatarOption> skinOptions = Arrays.asList(
                new AvatarOption(R.drawable.skin_white, R.drawable.skin_white),
                new AvatarOption(R.drawable.skin_black, R.drawable.skin_black)
        );
        AvatarOptionAdapter skinAdapter = new AvatarOptionAdapter(skinOptions, resId ->
                skinView.setImageResource(resId));
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
