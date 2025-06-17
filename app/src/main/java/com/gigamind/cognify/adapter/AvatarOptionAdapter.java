package com.gigamind.cognify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gigamind.cognify.R;

import java.util.List;

public class AvatarOptionAdapter extends RecyclerView.Adapter<AvatarOptionAdapter.ViewHolder> {

    public interface OnOptionClickListener {
        void onOptionClicked(int resId);
    }

    private final List<Integer> items;
    private final OnOptionClickListener listener;

    public AvatarOptionAdapter(List<Integer> items, OnOptionClickListener listener) {
        this.items = items;
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
        int resId = items.get(position);
        holder.imageView.setImageResource(resId);
        holder.imageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOptionClicked(resId);
            }
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
