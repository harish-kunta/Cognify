package com.gigamind.cognify.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gigamind.cognify.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    private RecyclerView leaderboardRecyclerView;
    private LeaderboardAdapter adapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Initialize views
        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView);
        tabLayout = findViewById(R.id.tabLayout);

        // Set up RecyclerView
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter();
        leaderboardRecyclerView.setAdapter(adapter);

        // Set up tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadLeaderboardData(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Load initial data
        loadLeaderboardData(0);
    }

    private void loadLeaderboardData(int gameMode) {
        // TODO: Replace with real data from Firebase or local storage
        List<LeaderboardItem> items = new ArrayList<>();
        
        // Dummy data
        if (gameMode == 0) { // Word Dash
            items.add(new LeaderboardItem("Alice", 850, 15));
            items.add(new LeaderboardItem("Bob", 720, 12));
            items.add(new LeaderboardItem("Charlie", 680, 8));
            items.add(new LeaderboardItem("David", 550, 5));
            items.add(new LeaderboardItem("Eve", 520, 3));
        } else { // Quick Math
            items.add(new LeaderboardItem("Frank", 100, 20));
            items.add(new LeaderboardItem("Grace", 90, 18));
            items.add(new LeaderboardItem("Henry", 80, 15));
            items.add(new LeaderboardItem("Ivy", 70, 10));
            items.add(new LeaderboardItem("Jack", 60, 7));
        }

        adapter.setItems(items);
    }

    private static class LeaderboardItem {
        String name;
        int score;
        int streak;

        LeaderboardItem(String name, int score, int streak) {
            this.name = name;
            this.score = score;
            this.streak = streak;
        }
    }

    private class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
        private List<LeaderboardItem> items = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LeaderboardItem item = items.get(position);
            holder.rankText.setText(String.valueOf(position + 1));
            holder.playerName.setText(item.name);
            holder.scoreText.setText(String.valueOf(item.score));
            holder.streakText.setText(String.format("🔥 %d days", item.streak));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        void setItems(List<LeaderboardItem> newItems) {
            items = newItems;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView rankText;
            TextView playerName;
            TextView scoreText;
            TextView streakText;

            ViewHolder(View itemView) {
                super(itemView);
                rankText = itemView.findViewById(R.id.rankText);
                playerName = itemView.findViewById(R.id.nameText);
                scoreText = itemView.findViewById(R.id.scoreText);
                streakText = itemView.findViewById(R.id.streakText);
            }
        }
    }
} 