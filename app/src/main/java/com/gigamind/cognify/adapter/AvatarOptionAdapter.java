package com.gigamind.cognify.adapter;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

import com.gigamind.cognify.R;

import java.util.List;

public class AvatarOptionAdapter extends RecyclerView.Adapter<AvatarOptionAdapter.ViewHolder> {

    public interface OnOptionClickListener {
        void onOptionClicked(int resId);
    }

    public static class AvatarOption {
        public final int iconResId;
        public final int applyResId;

        public AvatarOption(int iconResId, int applyResId) {
            this.iconResId = iconResId;
            this.applyResId = applyResId;
        }
    }

    private final List<AvatarOption> items;
    private final OnOptionClickListener listener;
    private int selectedPosition;

    public AvatarOptionAdapter(List<AvatarOption> items, OnOptionClickListener listener) {
        this(items, listener, 0);
    }

    public AvatarOptionAdapter(List<AvatarOption> items, OnOptionClickListener listener, int initialSelectedPosition) {
        this.items = items;
        this.listener = listener;
        this.selectedPosition = initialSelectedPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_avatar_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AvatarOption option = items.get(position);
        holder.imageView.setImageResource(option.iconResId);

        MaterialCardView card = (MaterialCardView) holder.itemView;
        if (position == selectedPosition) {
            card.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent));
            card.setStrokeWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
                    holder.itemView.getResources().getDisplayMetrics()));
        } else {
            card.setStrokeColor(Color.TRANSPARENT);
            card.setStrokeWidth(0);
        }

        holder.imageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOptionClicked(option.applyResId);
            }
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.optionImage);
        }
    }
}
