package com.example.viragram.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viragram.R;
import com.example.viragram.databinding.ItemStoryBinding;
import com.example.viragram.models.Story;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying campus stories in a horizontal RecyclerView.
 */
public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private final List<Story> storyList = new ArrayList<>();
    private final OnStoryClickListener listener;

    public interface OnStoryClickListener {
        void onStoryClick(Story story);
    }

    public StoryAdapter(OnStoryClickListener listener) {
        this.listener = listener;
    }

    public void setStories(List<Story> stories) {
        this.storyList.clear();
        if (stories != null) {
            this.storyList.addAll(stories);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStoryBinding binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Story story = storyList.get(position);
        holder.bind(story, listener);
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemStoryBinding binding;

        public StoryViewHolder(@NonNull ItemStoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Story story, OnStoryClickListener listener) {
            binding.tvStoryTitle.setText(story.getTitle());
            binding.ivStoryAvatar.setImageResource(story.getAvatarResId());

            if (story.isUserStory()) {
                binding.ivAddBadge.setVisibility(View.VISIBLE);
                binding.vStoryRing.setBackgroundResource(R.color.viragram_border);
            } else {
                binding.ivAddBadge.setVisibility(View.GONE);
                if (story.hasUnseenStory()) {
                    binding.vStoryRing.setBackgroundResource(R.drawable.bg_story_ring);
                } else {
                    binding.vStoryRing.setBackgroundResource(R.color.viragram_border);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStoryClick(story);
                }
            });
        }
    }
}