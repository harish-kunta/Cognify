package com.gigamind.cognify.ui.leaderboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gigamind.cognify.R;
import com.gigamind.cognify.databinding.ItemLeaderboardBinding;
import com.gigamind.cognify.data.firebase.FirebaseService;

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
                return oldItem.getTotalXP() == newItem.getTotalXP()
                        && oldItem.getRank() == newItem.getRank()
                        && oldItem.getName().equals(newItem.getName());
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

            // 3) Display Name
            binding.nameText.setText(item.getName());

            // 5) Points (Total XP)
            binding.pointsText.setText(String.valueOf(item.getTotalXP()));

            // 6) Highlight current user row
            String currentUid = FirebaseService.getInstance().getCurrentUser() != null
                    ? FirebaseService.getInstance().getCurrentUserId()
                    : "";
            boolean isCurrent = currentUid.equals(item.getUserId());
            binding.getRoot().setSelected(isCurrent);
        }

        private int getFlagDrawableId(@NonNull String code) {
            String lower = code.toLowerCase();
            // Example: code = "ID" → resource name "flag_id"
            int resId = binding.getRoot().getContext()
                    .getResources()
                    .getIdentifier("flag_" + lower, "drawable",
                            binding.getRoot().getContext().getPackageName());
            return resId;
        }

        /**
         * Return the badge drawable resource ID for a given badgeType ("bronze", "silver", "gold").
         */
        private int getBadgeDrawableId(@NonNull String badgeType) {
            return R.drawable.ic_badge;
        }
    }
} 
