package com.gigamind.cognify.ui.leaderboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gigamind.cognify.databinding.ItemLeaderboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LeaderboardAdapter extends ListAdapter<LeaderboardItem, LeaderboardAdapter.ViewHolder> {

    protected LeaderboardAdapter() {
        super(new DiffUtil.ItemCallback<LeaderboardItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull LeaderboardItem oldItem,
                                         @NonNull LeaderboardItem newItem) {
                return oldItem.getUserId().equals(newItem.getUserId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull LeaderboardItem oldItem,
                                            @NonNull LeaderboardItem newItem) {
                return oldItem.getScore() == newItem.getScore() && 
                       oldItem.getRank() == newItem.getRank() &&
                       oldItem.getName().equals(newItem.getName());
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderboardBinding binding = ItemLeaderboardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardItem item = getItem(position);
        holder.bind(item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLeaderboardBinding binding;

        ViewHolder(ItemLeaderboardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LeaderboardItem item) {
            binding.rankText.setText(String.valueOf(item.getRank()));
            binding.nameText.setText(item.getName());
            binding.scoreText.setText(String.valueOf(item.getScore()));

            // Highlight current user's score
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
            binding.getRoot().setSelected(currentUserId.equals(item.getUserId()));
        }
    }
} 