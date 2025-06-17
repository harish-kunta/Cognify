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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_maker);

        hairView = findViewById(R.id.hairView);
        RecyclerView recyclerView = findViewById(R.id.hairRecyclerView);

        List<Integer> hairOptions = Arrays.asList(
                R.drawable.hair_long,
                R.drawable.hair_bun,
                R.drawable.hair_curly
        );

        AvatarOptionAdapter adapter = new AvatarOptionAdapter(hairOptions, resId ->
                hairView.setImageResource(resId));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
    }
}
