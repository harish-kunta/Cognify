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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_maker);

        hairView = findViewById(R.id.hairView);
        eyesView = findViewById(R.id.eyesView);
        mouthView = findViewById(R.id.mouthView);
        skinView = findViewById(R.id.skinView);

        RecyclerView hairRecycler = findViewById(R.id.hairRecyclerView);
        RecyclerView eyesRecycler = findViewById(R.id.eyesRecyclerView);
        RecyclerView mouthRecycler = findViewById(R.id.mouthRecyclerView);
        RecyclerView skinRecycler = findViewById(R.id.skinRecyclerView);

        List<Integer> hairOptions = Arrays.asList(
                R.drawable.hair_long,
                R.drawable.hair_bun,
                R.drawable.hair_curly,
                R.drawable.hair_longbob
        );

        AvatarOptionAdapter hairAdapter = new AvatarOptionAdapter(hairOptions, resId ->
                hairView.setImageResource(resId));
        hairRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hairRecycler.setAdapter(hairAdapter);

        List<Integer> eyesOptions = Arrays.asList(
                R.drawable.eyes_default,
                R.drawable.eyes_happy,
                R.drawable.eyes_hearts
        );
        AvatarOptionAdapter eyesAdapter = new AvatarOptionAdapter(eyesOptions, resId ->
                eyesView.setImageResource(resId));
        eyesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        eyesRecycler.setAdapter(eyesAdapter);

        List<Integer> mouthOptions = Arrays.asList(
                R.drawable.mouth_smile,
                R.drawable.mouth_twinkle
        );
        AvatarOptionAdapter mouthAdapter = new AvatarOptionAdapter(mouthOptions, resId ->
                mouthView.setImageResource(resId));
        mouthRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mouthRecycler.setAdapter(mouthAdapter);

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
