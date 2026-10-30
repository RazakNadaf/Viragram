package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.viragram.R;
import com.example.viragram.adapters.ProfilePostAdapter;
import com.example.viragram.databinding.ActivityStudentProfileBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Public profile activity for viewed student profiles with persistent Firestore follow relationships and ViragramDebug diagnostics.
 */
public class StudentProfileActivity extends BaseActivity {

    private static final String TAG = "ViragramDebug";

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_COURSE = "extra_course";
    public static final String EXTRA_AVATAR_RES = "extra_avatar_res";
    public static final String EXTRA_TARGET_UID = "extra_target_uid";

    private ActivityStudentProfileBinding binding;
    private ProfilePostAdapter postAdapter;
    private boolean isFollowing = false;
    private String targetUid;
    private String myUid;
    private String myName = "Noor Fernandes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnStudentProfileBack.setOnClickListener(v -> finish());

        setupStudentDetails();
        setupPostsGrid();
    }

    private void setupStudentDetails() {
        Intent intent = getIntent();
        String name = intent.getStringExtra(EXTRA_NAME);
        String username = intent.getStringExtra(EXTRA_USERNAME);
        String course = intent.getStringExtra(EXTRA_COURSE);
        int avatarRes = intent.getIntExtra(EXTRA_AVATAR_RES, R.drawable.ic_avatar_2);

        String displayName = name != null ? name : "Campus Student";
        String displayUsername = username != null ? username : "student.unigoa";

        targetUid = intent.getStringExtra(EXTRA_TARGET_UID);
        if (targetUid == null || targetUid.isEmpty()) {
            targetUid = "student_" + displayUsername.replace("@", "").replace(".", "_");
        }

        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            myUid = currentUser.getUid();
            if (currentUser.getDisplayName() != null) {
                myName = currentUser.getDisplayName();
            }
        } else {
            myUid = "my_guest_uid";
        }

        binding.tvPublicHeaderTitle.setText(displayName);
        binding.tvPublicName.setText(displayName);
        binding.tvPublicUsername.setText("@" + displayUsername.replace("@", ""));
        binding.tvPublicCourse.setText((course != null ? course : "Goa University Student") + " • Goa University");
        binding.ivPublicAvatar.setImageResource(avatarRes);

        // Check if current user is following target student
        FirestoreManager.getInstance().checkIsFollowing(myUid, targetUid, task -> {
            boolean exists = task.isSuccessful() && task.getResult() != null && task.getResult().exists();
            isFollowing = exists;
            updateFollowButton();
            Log.d(TAG, "[ViragramDebug] users/" + myUid + "/following/" + targetUid + " EXISTS = " + exists);
        });

        binding.btnPublicFollow.setOnClickListener(v -> {
            Log.d(TAG, "[ViragramDebug] FOLLOW CURRENT UID = " + myUid);
            Log.d(TAG, "[ViragramDebug] FOLLOW TARGET UID = " + targetUid);

            boolean previousState = isFollowing;
            isFollowing = !isFollowing;
            updateFollowButton();

            if (isFollowing) {
                FirestoreManager.getInstance().followUser(myUid, myName, targetUid, displayName, task -> {
                    if (!task.isSuccessful()) {
                        isFollowing = previousState;
                        updateFollowButton();
                        Log.e(TAG, "[ViragramDebug] FOLLOW BATCH FAILED = " + (task.getException() != null ? task.getException().getMessage() : "Unknown"));
                    } else {
                        Log.d(TAG, "[ViragramDebug] FOLLOW BATCH SUCCESS");
                        readUpdatedCounts();
                    }
                });
            } else {
                FirestoreManager.getInstance().unfollowUser(myUid, targetUid, task -> {
                    if (!task.isSuccessful()) {
                        isFollowing = previousState;
                        updateFollowButton();
                        Log.e(TAG, "[ViragramDebug] UNFOLLOW BATCH FAILED = " + (task.getException() != null ? task.getException().getMessage() : "Unknown"));
                    } else {
                        Log.d(TAG, "[ViragramDebug] UNFOLLOW BATCH SUCCESS");
                        readUpdatedCounts();
                    }
                });
            }
        });

        binding.btnPublicMessage.setOnClickListener(v -> {
            Intent chatIntent = new Intent(StudentProfileActivity.this, ChatActivity.class);
            chatIntent.putExtra(ChatActivity.EXTRA_PARTICIPANT_NAME, displayName);
            chatIntent.putExtra(ChatActivity.EXTRA_USERNAME, displayUsername);
            chatIntent.putExtra(ChatActivity.EXTRA_AVATAR_RES, avatarRes);
            startActivity(chatIntent);
        });
    }

    private void readUpdatedCounts() {
        FirestoreManager.getInstance().getUserProfile(myUid, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Long following = task.getResult().getLong("followingCount");
                Log.d(TAG, "[ViragramDebug] MY FOLLOWING COUNT AFTER FOLLOW = " + following);
            }
        });

        FirestoreManager.getInstance().getUserProfile(targetUid, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Long followers = task.getResult().getLong("followerCount");
                Log.d(TAG, "[ViragramDebug] TARGET FOLLOWER COUNT AFTER FOLLOW = " + followers);
            }
        });

        FirebaseFirestore.getInstance().collection("users").document(myUid).collection("following").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Log.d(TAG, "[ViragramDebug] FOLLOWING DOCUMENT COUNT = " + task.getResult().size());
            }
        });

        FirebaseFirestore.getInstance().collection("users").document(targetUid).collection("followers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Log.d(TAG, "[ViragramDebug] FOLLOWERS DOCUMENT COUNT = " + task.getResult().size());
            }
        });
    }

    private void updateFollowButton() {
        if (isFollowing) {
            binding.btnPublicFollow.setText(R.string.btn_following);
            binding.btnPublicFollow.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.viragram_surface_light));
            binding.btnPublicFollow.setTextColor(ContextCompat.getColor(this, R.color.viragram_text_primary));
        } else {
            binding.btnPublicFollow.setText(R.string.btn_follow);
            binding.btnPublicFollow.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.viragram_purple_primary));
            binding.btnPublicFollow.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void setupPostsGrid() {
        postAdapter = new ProfilePostAdapter(post -> {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getPostId());
            intent.putExtra(PostDetailActivity.EXTRA_AUTHOR_NAME, binding.tvPublicName.getText().toString());
            intent.putExtra(PostDetailActivity.EXTRA_AUTHOR_COURSE, binding.tvPublicCourse.getText().toString());
            intent.putExtra(PostDetailActivity.EXTRA_AVATAR_RES, getIntent().getIntExtra(EXTRA_AVATAR_RES, R.drawable.ic_avatar_2));
            intent.putExtra(PostDetailActivity.EXTRA_IMAGE_RES, post.getPostImageResId());
            intent.putExtra(PostDetailActivity.EXTRA_CAPTION, post.getCaption());
            intent.putExtra(PostDetailActivity.EXTRA_LIKES, post.getLikeCount());
            intent.putExtra(PostDetailActivity.EXTRA_COMMENTS, post.getCommentCount());
            intent.putExtra(PostDetailActivity.EXTRA_TIME_AGO, post.getTimeAgo());
            startActivity(intent);
        });

        binding.rvPublicGrid.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvPublicGrid.setAdapter(postAdapter);

        // Load demo posts for viewed public profile
        List<Post> posts = new ArrayList<>();
        posts.add(new Post("sp1", "Student", "Unigoa", R.drawable.ic_avatar_2, R.drawable.bg_post_fest, "Campus cultural highlights 🎉", "1 day ago", 95, 12));
        posts.add(new Post("sp2", "Student", "Unigoa", R.drawable.ic_avatar_2, R.drawable.bg_post_coding, "Hackathon coding submission ready 💻", "3 days ago", 140, 19));
        posts.add(new Post("sp3", "Student", "Unigoa", R.drawable.ic_avatar_2, R.drawable.bg_post_campus, "Library reading corner 📚", "1 week ago", 72, 8));
        postAdapter.setPosts(posts);
    }
}