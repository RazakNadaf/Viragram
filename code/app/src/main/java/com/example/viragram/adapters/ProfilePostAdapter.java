package com.example.viragram.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.databinding.ItemProfilePostBinding;
import com.example.viragram.models.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying 3-column post grid in ProfileFragment using Glide for ImageKit remote media.
 */
public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.GridPostViewHolder> {

    private final List<Post> postList = new ArrayList<>();
    private final OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public ProfilePostAdapter(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.postList.clear();
        if (posts != null) {
            this.postList.addAll(posts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GridPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProfilePostBinding binding = ItemProfilePostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GridPostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GridPostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post, listener);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class GridPostViewHolder extends RecyclerView.ViewHolder {
        private final ItemProfilePostBinding binding;

        public GridPostViewHolder(@NonNull ItemProfilePostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Post post, OnPostClickListener listener) {
            Context context = itemView.getContext();

            if (post.getImageUriString() != null && !post.getImageUriString().isEmpty()) {
                Glide.with(context)
                        .load(post.getImageUriString())
                        .centerCrop()
                        .placeholder(R.drawable.bg_post_fest)
                        .error(R.drawable.bg_post_fest)
                        .into(binding.ivGridPostImage);
            } else if (post.getPostImageResId() != 0) {
                binding.ivGridPostImage.setImageResource(post.getPostImageResId());
            } else {
                binding.ivGridPostImage.setImageResource(R.drawable.bg_post_fest);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });
        }
    }
}