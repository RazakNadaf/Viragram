package com.example.viragram.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.activities.CommentsActivity;
import com.example.viragram.databinding.ItemPostBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Post;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for Viragram social media post feed with real-time Firestore likes, comments, and Glide media.
 */
public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_POST = 1;

    private final List<Post> postList = new ArrayList<>();
    private final StoryAdapter storyAdapter;

    public PostAdapter(StoryAdapter storyAdapter) {
        this.storyAdapter = storyAdapter;
    }

    public void setPosts(List<Post> posts) {
        this.postList.clear();
        if (posts != null) {
            this.postList.addAll(posts);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_POST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story_header, parent, false);
            return new HeaderViewHolder(headerView);
        }
        ItemPostBinding binding = ItemPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(storyAdapter);
        } else if (holder instanceof PostViewHolder) {
            Post post = postList.get(position - 1);
            ((PostViewHolder) holder).bind(post);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size() + 1; // +1 for stories header
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView rvStories;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            rvStories = itemView.findViewById(R.id.rv_stories);
        }

        public void bind(StoryAdapter storyAdapter) {
            if (rvStories != null && storyAdapter != null) {
                rvStories.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(
                        itemView.getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false
                ));
                rvStories.setAdapter(storyAdapter);
            }
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostBinding binding;
        private boolean isBookmarked = false;

        public PostViewHolder(@NonNull ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Post post) {
            Context context = itemView.getContext();
            FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
            String currentUid = currentUser != null ? currentUser.getUid() : "";

            if (post.getAuthorAvatarUrl() != null && !post.getAuthorAvatarUrl().isEmpty()) {
                Glide.with(context)
                        .load(post.getAuthorAvatarUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar_1)
                        .error(R.drawable.ic_avatar_1)
                        .into(binding.ivPostAuthorAvatar);
            } else {
                binding.ivPostAuthorAvatar.setImageResource(post.getAuthorAvatarResId() != 0 ? post.getAuthorAvatarResId() : R.drawable.ic_avatar_1);
            }

            binding.tvAuthorName.setText(post.getAuthorName());
            binding.tvAuthorCourse.setText(post.getAuthorCourseAndYear());

            if (post.getImageUriString() != null && !post.getImageUriString().isEmpty()) {
                Glide.with(context)
                        .load(post.getImageUriString())
                        .placeholder(R.drawable.bg_post_fest)
                        .error(R.drawable.bg_post_fest)
                        .into(binding.ivPostImage);
            } else {
                binding.ivPostImage.setImageResource(post.getPostImageResId() != 0 ? post.getPostImageResId() : R.drawable.bg_post_fest);
            }

            binding.tvTimeAgo.setText(post.getTimeAgo());

            // Check if liked in Firestore array
            boolean isLiked = post.getLikedBy() != null && post.getLikedBy().contains(currentUid);
            post.setLiked(isLiked);

            updateLikeUi(post);

            // Caption formatting (bold author name)
            String author = post.getAuthorName() != null ? post.getAuthorName() : "";
            String caption = post.getCaption() != null ? post.getCaption() : "";
            SpannableStringBuilder captionBuilder = new SpannableStringBuilder(author + "  " + caption);
            captionBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.tvCaption.setText(captionBuilder);

            // Comments link text
            String commentsText = "View all " + post.getCommentCount() + " comments";
            binding.tvViewComments.setText(commentsText);

            // Like Click with pulse animation and Firestore sync
            binding.btnLike.setOnClickListener(v -> {
                boolean newLikeState = !post.isLiked();
                post.setLiked(newLikeState);

                if (newLikeState) {
                    if (!post.getLikedBy().contains(currentUid)) {
                        post.getLikedBy().add(currentUid);
                    }
                    post.setLikeCount(post.getLikeCount() + 1);
                } else {
                    post.getLikedBy().remove(currentUid);
                    post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                }

                updateLikeUi(post);

                // Heart pulse scale animation
                binding.btnLike.animate().scaleX(1.25f).scaleY(1.25f).setDuration(100).withEndAction(() ->
                        binding.btnLike.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                ).start();

                FirestoreManager.getInstance().toggleLikePost(post.getPostId(), currentUid, newLikeState, task -> {
                    if (!task.isSuccessful()) {
                        Log.e("ViragramFirestore", "Like toggle failed for post: " + post.getPostId());
                    }
                });
            });

            // Follow Click
            binding.btnFollow.setOnClickListener(v -> {
                boolean newFollowState = !post.isFollowing();
                post.setFollowing(newFollowState);
                updateFollowUi(post);
            });

            // Comment Click
            View.OnClickListener commentClickListener = v -> {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.getPostId());
                intent.putExtra(CommentsActivity.EXTRA_AUTHOR_NAME, post.getAuthorName());
                context.startActivity(intent);
            };
            binding.btnComment.setOnClickListener(commentClickListener);
            binding.tvViewComments.setOnClickListener(commentClickListener);

            // Share Click
            binding.btnShare.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, post.getAuthorName() + " on Viragram: " + post.getCaption());
                context.startActivity(Intent.createChooser(shareIntent, "Share post via"));
            });

            // Bookmark Click
            binding.btnBookmark.setOnClickListener(v -> {
                isBookmarked = !isBookmarked;
                if (isBookmarked) {
                    Toast.makeText(context, "Post saved to your bookmarks", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Post removed from bookmarks", Toast.LENGTH_SHORT).show();
                }
            });

            // Three-dot menu
            binding.btnPostMenu.setOnClickListener(v -> showPostMenuBottomSheet(context, post));
        }

        private void updateLikeUi(Post post) {
            String likesStr = post.getLikeCount() + " likes";
            binding.tvLikeCount.setText(likesStr);

            if (post.isLiked()) {
                binding.btnLike.setImageResource(R.drawable.ic_like_filled_24);
                binding.btnLike.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.viragram_like_red));
            } else {
                binding.btnLike.setImageResource(R.drawable.ic_like_outline_24);
                binding.btnLike.clearColorFilter();
            }
        }

        private void updateFollowUi(Post post) {
            if (post.isFollowing()) {
                binding.btnFollow.setText(R.string.btn_following);
                binding.btnFollow.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.viragram_text_secondary));
                binding.btnFollow.setStrokeColorResource(R.color.viragram_border);
            } else {
                binding.btnFollow.setText(R.string.btn_follow);
                binding.btnFollow.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.viragram_purple_primary));
                binding.btnFollow.setStrokeColorResource(R.color.viragram_purple_primary);
            }
        }

        private void showPostMenuBottomSheet(Context context, Post post) {
            BottomSheetDialog dialog = new BottomSheetDialog(context);
            View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_post_menu, null);

            TextView btnSave = view.findViewById(R.id.btn_menu_save);
            TextView btnReport = view.findViewById(R.id.btn_menu_report);
            TextView btnCancel = view.findViewById(R.id.btn_menu_cancel);

            btnSave.setOnClickListener(v -> {
                Toast.makeText(context, "Post saved to your bookmarks", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            btnReport.setOnClickListener(v -> {
                Toast.makeText(context, "Post reported to campus moderators", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(view);
            dialog.show();
        }
    }
}