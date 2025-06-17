package com.gigamind.cognify.ui.avatar;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;

import com.gigamind.cognify.R;
import com.gigamind.cognify.adapter.AvatarOptionAdapter;

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

        List<Integer> hairOptions = Arrays.asList(
                R.drawable.hair_long,
                R.drawable.hair_bun,
                R.drawable.hair_curly,
                R.drawable.hair_longbob,
                R.drawable.hair_nottoolong,
                R.drawable.hair_longhairstraight2,
                R.drawable.hair_longhaircurvy
        );

        AvatarOptionAdapter hairAdapter = new AvatarOptionAdapter(hairOptions, resId ->
                hairView.setImageResource(resId));
        hairRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hairRecycler.setAdapter(hairAdapter);

        List<Integer> eyesOptions = Arrays.asList(
                R.drawable.eyes_default,
                R.drawable.eyes_happy,
                R.drawable.eyes_hearts,
                R.drawable.eyes_dizzy,
                R.drawable.eyes_wink,
                R.drawable.eyes_surprised,
                R.drawable.eyes_winkwacky
        );
        AvatarOptionAdapter eyesAdapter = new AvatarOptionAdapter(eyesOptions, resId ->
                eyesView.setImageResource(resId));
        eyesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        eyesRecycler.setAdapter(eyesAdapter);

        List<Integer> mouthOptions = Arrays.asList(
                R.drawable.mouth_smile,
                R.drawable.mouth_twinkle,
                R.drawable.mouth_tongue,
                R.drawable.mouth_serious,
                R.drawable.mouth_grimace
        );
        AvatarOptionAdapter mouthAdapter = new AvatarOptionAdapter(mouthOptions, resId ->
                mouthView.setImageResource(resId));
        mouthRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mouthRecycler.setAdapter(mouthAdapter);

        List<Integer> accessoriesOptions = Arrays.asList(
                R.drawable.acc_earphones,
                R.drawable.acc_earring1,
                R.drawable.acc_earring2
        );
        AvatarOptionAdapter accessoriesAdapter = new AvatarOptionAdapter(accessoriesOptions, resId ->
                accessoriesView.setImageResource(resId));
        accessoriesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        accessoriesRecycler.setAdapter(accessoriesAdapter);

        List<Integer> clothesOptions = Arrays.asList(
                R.drawable.clothes_blazer,
                R.drawable.clothes_overall,
                R.drawable.clothes_hoodie
        );
        AvatarOptionAdapter clothesAdapter = new AvatarOptionAdapter(clothesOptions, resId ->
                clothesView.setImageResource(resId));
        clothesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        clothesRecycler.setAdapter(clothesAdapter);

        List<Integer> eyebrowsOptions = Arrays.asList(
                R.drawable.eyebrows_default,
                R.drawable.eyebrows_angry,
                R.drawable.eyebrows_raised
        );
        AvatarOptionAdapter eyebrowsAdapter = new AvatarOptionAdapter(eyebrowsOptions, resId ->
                eyebrowsView.setImageResource(resId));
        eyebrowsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        eyebrowsRecycler.setAdapter(eyebrowsAdapter);

        List<Integer> facialHairOptions = Arrays.asList(
                R.drawable.facialhair_magnum,
                R.drawable.facialhair_fancy,
                R.drawable.facialhair_light
        );
        AvatarOptionAdapter facialHairAdapter = new AvatarOptionAdapter(facialHairOptions, resId ->
                facialHairView.setImageResource(resId));
        facialHairRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        facialHairRecycler.setAdapter(facialHairAdapter);

        List<Integer> glassesOptions = Arrays.asList(
                R.drawable.glasses_rambo,
                R.drawable.glasses_nerd,
                R.drawable.glasses_fancy
        );
        AvatarOptionAdapter glassesAdapter = new AvatarOptionAdapter(glassesOptions, resId ->
                glassesView.setImageResource(resId));
        glassesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        glassesRecycler.setAdapter(glassesAdapter);

        List<Integer> tattooOptions = Arrays.asList(
                R.drawable.tattoo_harry,
                R.drawable.tattoo_tribal,
                R.drawable.tattoo_airbender
        );
        AvatarOptionAdapter tattooAdapter = new AvatarOptionAdapter(tattooOptions, resId ->
                tattooView.setImageResource(resId));
        tattooRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tattooRecycler.setAdapter(tattooAdapter);

        List<Integer> skinOptions = Arrays.asList(
                R.drawable.skin_white,
                R.drawable.skin_black
        );
        AvatarOptionAdapter skinAdapter = new AvatarOptionAdapter(skinOptions, resId ->
                skinView.setImageResource(resId));
        skinRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        skinRecycler.setAdapter(skinAdapter);
    }
}
