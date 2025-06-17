package com.gigamind.cognify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gigamind.cognify.R;

/**
 * Reusable adapter for avatar customization options.
 */
public class AvatarOptionAdapter extends RecyclerView.Adapter<AvatarOptionAdapter.ViewHolder> {

    public interface OnOptionSelectedListener {
        void onOptionSelected(int position);
    }

    private final int[] drawables;
    private int selectedIndex = 0;
    private final OnOptionSelectedListener listener;

    public AvatarOptionAdapter(int[] drawables, OnOptionSelectedListener listener) {
        this.drawables = drawables;
        this.listener = listener;
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
        holder.bind(drawables[position], position == selectedIndex);
    }

    @Override
    public int getItemCount() {
        return drawables.length;
    }

    public void setSelectedIndex(int index) {
        int prev = selectedIndex;
        selectedIndex = index;
        notifyItemChanged(prev);
        notifyItemChanged(index);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.optionImage);
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    setSelectedIndex(pos);
                    listener.onOptionSelected(pos);
                }
            });
        }

        void bind(int resId, boolean selected) {
            image.setImageResource(resId);
            itemView.setSelected(selected);
        }
    }
}
