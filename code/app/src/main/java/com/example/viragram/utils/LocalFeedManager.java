package com.example.viragram.utils;

import com.example.viragram.R;
import com.example.viragram.models.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton repository manager for local feed posts in Viragram.
 */
public class LocalFeedManager {

    private static LocalFeedManager instance;
    private final List<Post> feedPosts = new ArrayList<>();

    private LocalFeedManager() {
        initDefaultPosts();
    }

    public static synchronized LocalFeedManager getInstance() {
        if (instance == null) {
            instance = new LocalFeedManager();
        }
        return instance;
    }

    private void initDefaultPosts() {
        feedPosts.clear();
        feedPosts.add(new Post(
                "p1",
                "Rahul Patil",
                "MCA • 1st Year",
                R.drawable.ic_avatar_1,
                R.drawable.bg_post_fest,
                "Great day at the college fest! 🔥 Unigoa cultural vibes were electric tonight.",
                "2 hours ago",
                125,
                18
        ));

        feedPosts.add(new Post(
                "p2",
                "Computer Science Club",
                "Goa University",
                R.drawable.ic_avatar_4,
                R.drawable.bg_post_coding,
                "Hackathon 2026 is officially live! 🚀 Registrations close tomorrow midnight.",
                "4 hours ago",
                256,
                32
        ));

        feedPosts.add(new Post(
                "p3",
                "Priya Naik",
                "MSc Data Science • 2nd Year",
                R.drawable.ic_avatar_2,
                R.drawable.bg_post_cultural,
                "Shining stars at the annual Goa University cultural evening performance 🌟🎶",
                "6 hours ago",
                89,
                12
        ));

        feedPosts.add(new Post(
                "p4",
                "Sports Club GU",
                "Campus Athletics",
                R.drawable.ic_avatar_5,
                R.drawable.bg_post_sports,
                "Inter-department football tournament finals set for Friday 4 PM! ⚽ Come cheer!",
                "1 day ago",
                310,
                45
        ));

        feedPosts.add(new Post(
                "p5",
                "Aniket Sharma",
                "B.Tech CSE • Final Year",
                R.drawable.ic_avatar_3,
                R.drawable.bg_post_campus,
                "Late night study sessions at the central library before mid-terms 📚💡",
                "2 days ago",
                67,
                8
        ));
    }

    public List<Post> getFeedPosts() {
        return feedPosts;
    }

    public void addPostToTop(Post newPost) {
        if (newPost != null) {
            feedPosts.add(0, newPost);
        }
    }
}