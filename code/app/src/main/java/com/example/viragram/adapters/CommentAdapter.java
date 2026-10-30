package com.example.viragram.adapters;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.databinding.ItemCommentBinding;
import com.example.viragram.models.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying real-time post comments using Glide for avatars.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> commentList = new ArrayList<>();

    public void setComments(List<Comment> comments) {
        this.commentList.clear();
        if (comments != null) {
            this.commentList.addAll(comments);
        }
        notifyDataSetChanged();
    }

    public void addComment(Comment comment) {
        if (comment != null) {
            this.commentList.add(comment);
            notifyItemInserted(this.commentList.size() - 1);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(commentList.get(position));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;

        public CommentViewHolder(@NonNull ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Comment comment) {
            Context context = itemView.getContext();

            if (comment.getAuthorAvatarUrl() != null && !comment.getAuthorAvatarUrl().isEmpty()) {
                Glide.with(context)
                        .load(comment.getAuthorAvatarUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar_1)
                        .error(R.drawable.ic_avatar_1)
                        .into(binding.ivCommentAvatar);
            } else {
                binding.ivCommentAvatar.setImageResource(comment.getAuthorAvatarResId() != 0 ? comment.getAuthorAvatarResId() : R.drawable.ic_avatar_1);
            }

            binding.tvCommentTime.setText(comment.getTimeAgo());

            String name = comment.getAuthorName() != null ? comment.getAuthorName() : "Campus Student";
            String text = comment.getCommentText() != null ? comment.getCommentText() : "";

            SpannableStringBuilder builder = new SpannableStringBuilder(name + "  " + text);
            builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.tvCommentBody.setText(builder);
        }
    }
}