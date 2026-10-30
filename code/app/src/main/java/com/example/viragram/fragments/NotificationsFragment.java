package com.example.viragram.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.viragram.R;
import com.example.viragram.activities.ClubDetailActivity;
import com.example.viragram.activities.CommentsActivity;
import com.example.viragram.activities.EventDetailActivity;
import com.example.viragram.activities.PostDetailActivity;
import com.example.viragram.activities.StudentProfileActivity;
import com.example.viragram.adapters.NotificationAdapter;
import com.example.viragram.databinding.FragmentNotificationsBinding;
import com.example.viragram.models.NotificationItem;
import com.example.viragram.utils.ViragramNotificationManager;

import java.util.List;

/**
 * Fragment displaying user activity, likes, comments, and campus updates.
 */
public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupActions();
        loadNotifications();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this::onNotificationClicked);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void setupActions() {
        binding.btnMarkAllRead.setOnClickListener(v -> {
            ViragramNotificationManager.getInstance().markAllAsRead();
            loadNotifications();
            Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadNotifications() {
        List<NotificationItem> list = ViragramNotificationManager.getInstance().getNotifications();

        if (list == null || list.isEmpty()) {
            binding.rvNotifications.setVisibility(View.GONE);
            binding.layoutNotificationsEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.rvNotifications.setVisibility(View.VISIBLE);
            binding.layoutNotificationsEmptyState.setVisibility(View.GONE);
            adapter.setNotifications(list);
        }
    }

    private void onNotificationClicked(NotificationItem item) {
        if (getContext() == null || item == null) return;

        if (item.getRelatedPostId() != null) {
            if (item.getType() == NotificationItem.NotificationType.COMMENT) {
                Intent intent = new Intent(getContext(), CommentsActivity.class);
                intent.putExtra(CommentsActivity.EXTRA_POST_ID, item.getRelatedPostId());
                intent.putExtra(CommentsActivity.EXTRA_AUTHOR_NAME, item.getActorName());
                startActivity(intent);
            } else {
                Intent intent = new Intent(getContext(), PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, item.getRelatedPostId());
                intent.putExtra(PostDetailActivity.EXTRA_AUTHOR_NAME, item.getActorName());
                intent.putExtra(PostDetailActivity.EXTRA_AUTHOR_COURSE, "Goa University Student");
                intent.putExtra(PostDetailActivity.EXTRA_AVATAR_RES, item.getActorAvatarResId());
                intent.putExtra(PostDetailActivity.EXTRA_IMAGE_RES, item.getPostThumbnailResId());
                intent.putExtra(PostDetailActivity.EXTRA_CAPTION, item.getMessage());
                startActivity(intent);
            }
        } else if (item.getRelatedClubId() != null) {
            Intent intent = new Intent(getContext(), ClubDetailActivity.class);
            intent.putExtra(ClubDetailActivity.EXTRA_CLUB_NAME, item.getActorName());
            intent.putExtra(ClubDetailActivity.EXTRA_CLUB_DESC, item.getMessage());
            intent.putExtra(ClubDetailActivity.EXTRA_AVATAR_RES, item.getActorAvatarResId());
            startActivity(intent);
        } else if (item.getRelatedEventId() != null) {
            Intent intent = new Intent(getContext(), EventDetailActivity.class);
            intent.putExtra(EventDetailActivity.EXTRA_TITLE, item.getActorName());
            intent.putExtra(EventDetailActivity.EXTRA_DESC, item.getMessage());
            intent.putExtra(EventDetailActivity.EXTRA_IMAGE_RES, item.getPostThumbnailResId() != 0 ? item.getPostThumbnailResId() : R.drawable.bg_post_fest);
            startActivity(intent);
        } else if (item.getType() == NotificationItem.NotificationType.FOLLOW) {
            Intent intent = new Intent(getContext(), StudentProfileActivity.class);
            intent.putExtra(StudentProfileActivity.EXTRA_NAME, item.getActorName());
            intent.putExtra(StudentProfileActivity.EXTRA_USERNAME, item.getActorUsername());
            intent.putExtra(StudentProfileActivity.EXTRA_AVATAR_RES, item.getActorAvatarResId());
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}