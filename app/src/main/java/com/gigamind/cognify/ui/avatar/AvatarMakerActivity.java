package com.gigamind.cognify.ui.avatar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.gigamind.cognify.R;

public class AvatarMakerActivity extends AppCompatActivity {
    private ImageView hairView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_maker);

        hairView = findViewById(R.id.hairView);
        Button longHairBtn = findViewById(R.id.longHairButton);
        Button hairBunBtn = findViewById(R.id.hairBunButton);
        Button hairCurlyBtn = findViewById(R.id.hairCurlyButton);

        longHairBtn.setOnClickListener(v -> hairView.setImageResource(R.drawable.hair_long));
        hairBunBtn.setOnClickListener(v -> hairView.setImageResource(R.drawable.hair_bun));
        hairCurlyBtn.setOnClickListener(v -> hairView.setImageResource(R.drawable.hair_curly));
    }
}
