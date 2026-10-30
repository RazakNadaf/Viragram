package com.example.viragram.utils;

import com.example.viragram.R;
import com.example.viragram.models.NotificationItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton notification repository manager for Viragram activity feed.
 */
public class ViragramNotificationManager {

    private static ViragramNotificationManager instance;
    private final List<NotificationItem> notifications = new ArrayList<>();

    private ViragramNotificationManager() {
        initDemoNotifications();
    }

    public static synchronized ViragramNotificationManager getInstance() {
        if (instance == null) {
            instance = new ViragramNotificationManager();
        }
        return instance;
    }

    private void initDemoNotifications() {
        notifications.clear();

        // TODAY
        NotificationItem n1 = new NotificationItem("n1", NotificationItem.NotificationType.LIKE, "Rahul Patil", "rahul.patil", "liked your post.", "10m ago", R.drawable.ic_avatar_1, R.drawable.bg_post_fest, "Today");
        n1.setRelatedPostId("p1");

        NotificationItem n2 = new NotificationItem("n2", NotificationItem.NotificationType.COMMENT, "Noor Fernandes", "noor.f", "commented on your post: 'Great photos!'", "45m ago", R.drawable.ic_avatar_2, R.drawable.bg_post_coding, "Today");
        n2.setRelatedPostId("p2");

        NotificationItem n3 = new NotificationItem("n3", NotificationItem.NotificationType.FOLLOW, "Arjun Naik", "arjun.n", "started following you.", "2h ago", R.drawable.ic_avatar_3, 0, "Today");

        // THIS WEEK
        NotificationItem n4 = new NotificationItem("n4", NotificationItem.NotificationType.CLUB, "Computer Science Club", "cs.club", "posted a new update: 'Hackathon 2026 starts Friday!'", "1d ago", R.drawable.ic_avatar_4, R.drawable.bg_post_coding, "This Week");
        n4.setRelatedClubId("c1");

        NotificationItem n5 = new NotificationItem("n5", NotificationItem.NotificationType.EVENT, "Goa University Events", "events.unigoa", "Hackathon 2026 is coming up this Friday", "2d ago", R.drawable.ic_avatar_6, R.drawable.bg_post_fest, "This Week");
        n5.setRelatedEventId("e2");

        NotificationItem n6 = new NotificationItem("n6", NotificationItem.NotificationType.CLUB, "Sports Club GU", "sports.gu", "has a new announcement: 'Football finals set for 4 PM'", "3d ago", R.drawable.ic_avatar_5, R.drawable.bg_post_sports, "This Week");
        n6.setRelatedClubId("c2");

        // EARLIER
        NotificationItem n7 = new NotificationItem("n7", NotificationItem.NotificationType.LIKE, "Priya Naik", "priya.n", "liked your post.", "5d ago", R.drawable.ic_avatar_2, R.drawable.bg_post_cultural, "Earlier");
        n7.setRelatedPostId("p3");
        n7.setRead(true);

        NotificationItem n8 = new NotificationItem("n8", NotificationItem.NotificationType.FOLLOW, "Ananya Sawant", "ananya.s", "started following you.", "1w ago", R.drawable.ic_avatar_6, 0, "Earlier");
        n8.setRead(true);

        notifications.add(n1);
        notifications.add(n2);
        notifications.add(n3);
        notifications.add(n4);
        notifications.add(n5);
        notifications.add(n6);
        notifications.add(n7);
        notifications.add(n8);
    }

    public List<NotificationItem> getNotifications() {
        return notifications;
    }

    public void markAsRead(String id) {
        for (NotificationItem n : notifications) {
            if (n.getId().equals(id)) {
                n.setRead(true);
                break;
            }
        }
    }

    public void markAllAsRead() {
        for (NotificationItem n : notifications) {
            n.setRead(true);
        }
    }

    public boolean hasUnread() {
        for (NotificationItem n : notifications) {
            if (!n.isRead()) {
                return true;
            }
        }
        return false;
    }
}