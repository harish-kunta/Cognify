package com.gigamind.cognify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.gigamind.cognify.R;

/**
 * Adapter to display each String (“found word”) as a small rounded pill.
 */
public class FoundWordsAdapter extends ListAdapter<String, FoundWordsAdapter.WordViewHolder> {

    public FoundWordsAdapter() {
        super(new DiffUtil.ItemCallback<String>() {
            @Override
            public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                // If two Strings are equal, consider them the same item.
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate our item_found_word.xml layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_found_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        String word = getItem(position);
        holder.bind(word);
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        private final TextView wordText;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.wordText);
        }

        void bind(String word) {
            wordText.setText(word);
        }
    }
}

