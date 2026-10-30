package com.example.viragram.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viragram.R;
import com.example.viragram.activities.ClubDetailActivity;
import com.example.viragram.activities.CommentsActivity;
import com.example.viragram.activities.EventDetailActivity;
import com.example.viragram.activities.PostDetailActivity;
import com.example.viragram.activities.StudentProfileActivity;
import com.example.viragram.databinding.ItemNotificationBinding;
import com.example.viragram.models.NotificationItem;
import com.example.viragram.utils.ViragramNotificationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying activity feed notifications on Viragram.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<NotificationItem> notificationList = new ArrayList<>();
    private final OnNotificationClickListener clickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem notification);
    }

    public NotificationAdapter(OnNotificationClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setNotifications(List<NotificationItem> notifications) {
        this.notificationList.clear();
        if (notifications != null) {
            this.notificationList.addAll(notifications);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);
        boolean showHeader = position == 0 || !item.getSectionHeader().equals(notificationList.get(position - 1).getSectionHeader());
        holder.bind(item, showHeader, clickListener);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        public NotificationViewHolder(@NonNull ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(NotificationItem item, boolean showHeader, OnNotificationClickListener clickListener) {
            Context context = itemView.getContext();

            // Section Header
            if (showHeader) {
                binding.tvNotificationSectionHeader.setVisibility(View.VISIBLE);
                binding.tvNotificationSectionHeader.setText(item.getSectionHeader());
            } else {
                binding.tvNotificationSectionHeader.setVisibility(View.GONE);
            }

            // Actor Avatar
            binding.ivNotificationAvatar.setImageResource(item.getActorAvatarResId() != 0 ? item.getActorAvatarResId() : R.drawable.ic_avatar_1);

            // Badge Icon Overlay
            setupTypeBadge(item.getType());

            // Message Formatting (bold actor name)
            String name = item.getActorName() != null ? item.getActorName() : "User";
            String msg = item.getMessage() != null ? item.getMessage() : "";
            SpannableStringBuilder builder = new SpannableStringBuilder(name + " " + msg);
            builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.tvNotificationMessage.setText(builder);

            // Time
            binding.tvNotificationTime.setText(item.getTimeAgo());

            // Post Thumbnail
            if (item.getPostThumbnailResId() != 0) {
                binding.ivNotificationThumbnail.setVisibility(View.VISIBLE);
                binding.ivNotificationThumbnail.setImageResource(item.getPostThumbnailResId());
            } else {
                binding.ivNotificationThumbnail.setVisibility(View.GONE);
            }

            // Unread State
            if (item.isRead()) {
                binding.vUnreadDot.setVisibility(View.GONE);
                binding.layoutNotificationRow.setBackgroundColor(ContextCompat.getColor(context, R.color.viragram_surface_light));
            } else {
                binding.vUnreadDot.setVisibility(View.VISIBLE);
                binding.layoutNotificationRow.setBackgroundColor(ContextCompat.getColor(context, R.color.viragram_purple_light));
            }

            // Row Click
            itemView.setOnClickListener(v -> {
                item.setRead(true);
                ViragramNotificationManager.getInstance().markAsRead(item.getId());

                binding.vUnreadDot.setVisibility(View.GONE);
                binding.layoutNotificationRow.setBackgroundColor(ContextCompat.getColor(context, R.color.viragram_surface_light));

                if (clickListener != null) {
                    clickListener.onNotificationClick(item);
                } else {
                    navigateToDestination(context, item);
                }
            });
        }

        private void setupTypeBadge(NotificationItem.NotificationType type) {
            if (type == null) {
                binding.ivTypeBadge.setVisibility(View.GONE);
                return;
            }
            binding.ivTypeBadge.setVisibility(View.VISIBLE);

            switch (type) {
                case LIKE:
                    binding.ivTypeBadge.setImageResource(R.drawable.ic_like_filled_24);
                    break;
                case COMMENT:
                    binding.ivTypeBadge.setImageResource(R.drawable.ic_comment_24);
                    break;
                case FOLLOW:
                    binding.ivTypeBadge.setImageResource(R.drawable.ic_person_24);
                    break;
                case CLUB:
                    binding.ivTypeBadge.setImageResource(R.drawable.ic_group_24);
                    break;
                case EVENT:
                    binding.ivTypeBadge.setImageResource(R.drawable.ic_calendar_24);
                    break;
            }
        }

        private void navigateToDestination(Context context, NotificationItem item) {
            if (item.getRelatedPostId() != null) {
                if (item.getType() == NotificationItem.NotificationType.COMMENT) {
                    Intent intent = new Intent(context, CommentsActivity.class);
                    intent.putExtra(CommentsActivity.EXTRA_POST_ID, item.getRelatedPostId());
                    intent.putExtra(CommentsActivity.EXTRA_AUTHOR_NAME, item.getActorName());
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra(PostDetailActivity.EXTRA_POST_ID, item.getRelatedPostId());
                    intent.putExtra(PostDetailActivity.EXTRA_AUTHOR_NAME, item.getActorName());
                    intent.putExtra(PostDetailActivity.EXTRA_AUTHOR_COURSE, "Goa University Student");
                    intent.putExtra(PostDetailActivity.EXTRA_AVATAR_RES, item.getActorAvatarResId());
                    intent.putExtra(PostDetailActivity.EXTRA_IMAGE_RES, item.getPostThumbnailResId());
                    intent.putExtra(PostDetailActivity.EXTRA_CAPTION, item.getMessage());
                    context.startActivity(intent);
                }
            } else if (item.getRelatedClubId() != null) {
                Intent intent = new Intent(context, ClubDetailActivity.class);
                intent.putExtra(ClubDetailActivity.EXTRA_CLUB_NAME, item.getActorName());
                intent.putExtra(ClubDetailActivity.EXTRA_CLUB_DESC, item.getMessage());
                intent.putExtra(ClubDetailActivity.EXTRA_AVATAR_RES, item.getActorAvatarResId());
                context.startActivity(intent);
            } else if (item.getRelatedEventId() != null) {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra(EventDetailActivity.EXTRA_TITLE, item.getActorName());
                intent.putExtra(EventDetailActivity.EXTRA_DESC, item.getMessage());
                intent.putExtra(EventDetailActivity.EXTRA_IMAGE_RES, item.getPostThumbnailResId() != 0 ? item.getPostThumbnailResId() : R.drawable.bg_post_fest);
                context.startActivity(intent);
            } else if (item.getType() == NotificationItem.NotificationType.FOLLOW) {
                Intent intent = new Intent(context, StudentProfileActivity.class);
                intent.putExtra(StudentProfileActivity.EXTRA_NAME, item.getActorName());
                intent.putExtra(StudentProfileActivity.EXTRA_USERNAME, item.getActorUsername());
                intent.putExtra(StudentProfileActivity.EXTRA_AVATAR_RES, item.getActorAvatarResId());
                context.startActivity(intent);
            }
        }
    }
}