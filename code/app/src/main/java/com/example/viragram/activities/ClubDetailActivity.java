package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.viragram.R;
import com.example.viragram.adapters.PostAdapter;
import com.example.viragram.adapters.StoryAdapter;
import com.example.viragram.databinding.ActivityClubDetailBinding;
import com.example.viragram.models.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity displaying details, events, and feed for a campus club.
 */
public class ClubDetailActivity extends BaseActivity {

    public static final String EXTRA_CLUB_NAME = "extra_club_name";
    public static final String EXTRA_CLUB_DESC = "extra_club_desc";
    public static final String EXTRA_CLUB_MEMBERS = "extra_club_members";
    public static final String EXTRA_AVATAR_RES = "extra_avatar_res";

    private ActivityClubDetailBinding binding;
    private PostAdapter postAdapter;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClubDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnClubBack.setOnClickListener(v -> finish());

        setupClubInfo();
        setupClubFeed();
    }

    private void setupClubInfo() {
        Intent intent = getIntent();
        String clubName = intent.getStringExtra(EXTRA_CLUB_NAME);
        String clubDesc = intent.getStringExtra(EXTRA_CLUB_DESC);
        int memberCount = intent.getIntExtra(EXTRA_CLUB_MEMBERS, 128);
        int avatarRes = intent.getIntExtra(EXTRA_AVATAR_RES, R.drawable.ic_avatar_4);

        String name = clubName != null ? clubName : "Campus Club";
        binding.tvClubToolbarTitle.setText(name);
        binding.tvClubDetailName.setText(name);
        binding.tvClubDetailDesc.setText(clubDesc != null ? clubDesc : "Official student club at Goa University.");
        binding.tvClubDetailMembers.setText(memberCount + " Active Student Members");
        binding.ivClubDetailAvatar.setImageResource(avatarRes);

        binding.btnClubDetailFollow.setOnClickListener(v -> {
            isFollowing = !isFollowing;
            if (isFollowing) {
                binding.btnClubDetailFollow.setText(R.string.btn_following);
                binding.btnClubDetailFollow.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.viragram_surface_light));
                binding.btnClubDetailFollow.setTextColor(ContextCompat.getColor(this, R.color.viragram_text_primary));
                Toast.makeText(this, "Joined " + name, Toast.LENGTH_SHORT).show();
            } else {
                binding.btnClubDetailFollow.setText(R.string.btn_follow);
                binding.btnClubDetailFollow.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.viragram_purple_primary));
                binding.btnClubDetailFollow.setTextColor(ContextCompat.getColor(this, R.color.white));
            }
        });
    }

    private void setupClubFeed() {
        StoryAdapter dummyStoryAdapter = new StoryAdapter(story -> {});
        postAdapter = new PostAdapter(dummyStoryAdapter);

        binding.rvClubPosts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvClubPosts.setAdapter(postAdapter);

        String clubName = getIntent().getStringExtra(EXTRA_CLUB_NAME);
        int avatarRes = getIntent().getIntExtra(EXTRA_AVATAR_RES, R.drawable.ic_avatar_4);

        List<Post> clubPosts = new ArrayList<>();
        clubPosts.add(new Post("cp1", clubName != null ? clubName : "Computer Science Club", "Goa University", avatarRes, R.drawable.bg_post_coding, "Hackathon 2026 is officially live! Registrations open to all departments.", "4 hours ago", 256, 32));
        clubPosts.add(new Post("cp2", clubName != null ? clubName : "Computer Science Club", "Goa University", avatarRes, R.drawable.bg_post_fest, "Tech symposium inaugural ceremony highlights!", "2 days ago", 189, 15));

        postAdapter.setPosts(clubPosts);
    }
}