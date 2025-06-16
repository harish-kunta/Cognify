package com.gigamind.cognify.ui.trophy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gigamind.cognify.R;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.util.BadgeUtils;

/** Simple activity showing unlocked badge tiers. */
public class TrophyRoomActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophy_room);

        LinearLayout list = findViewById(R.id.trophyList);
        int totalXp = new UserRepository(this).getTotalXP();
        int unlockedIdx = BadgeUtils.badgeIndexForXp(totalXp);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < BadgeUtils.NAMES.length; i++) {
            LinearLayout item = (LinearLayout) inflater.inflate(R.layout.item_trophy, list, false);
            ImageView icon = item.findViewById(R.id.trophyIcon);
            TextView name = item.findViewById(R.id.trophyName);
            icon.setImageResource(BadgeUtils.badgeIconResId(i));
            name.setText(BadgeUtils.NAMES[i]);
            item.setAlpha(i <= unlockedIdx ? 1f : 0.3f);
            list.addView(item);
        }
    }
}
