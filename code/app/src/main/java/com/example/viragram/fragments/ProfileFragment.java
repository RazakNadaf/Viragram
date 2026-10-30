package com.example.viragram.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.activities.EditProfileActivity;
import com.example.viragram.activities.PostDetailActivity;
import com.example.viragram.activities.ProfileSettingsActivity;
import com.example.viragram.adapters.ProfilePostAdapter;
import com.example.viragram.databinding.FragmentProfileBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Post;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying profile page for presentation demo, loading real user posts & ImageKit avatars.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ViragramProfile";

    private FragmentProfileBinding binding;
    private ProfilePostAdapter gridAdapter;
    private ListenerRegistration profileListener;
    private ListenerRegistration userPostsListener;

    private String currentName = "Campus Student";
    private String currentUsername = "student";
    private String currentBio = "";
    private String currentCourse = "Goa University Student";
    private String currentYear = "1st Year";
    private String currentUniversity = "Goa University";
    private String currentAvatarUrl = "";
    
    private long postCount = 0;
    private long followerCount = 0;
    private long followingCount = 0;

    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadFirestoreProfile();
                    loadUserPosts();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupHeaderAndStats();
        setupGridRecyclerView();
        setupTabs();
        loadFirestoreProfile();
        loadUserPosts();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFirestoreProfile();
        loadUserPosts();
    }

    private void setupHeaderAndStats() {
        updateProfileUi();

        // Settings click
        binding.btnProfileSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProfileSettingsActivity.class);
            startActivity(intent);
        });

        // Edit Profile click
        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            intent.putExtra(EditProfileActivity.EXTRA_NAME, currentName);
            intent.putExtra(EditProfileActivity.EXTRA_USERNAME, currentUsername);
            intent.putExtra(EditProfileActivity.EXTRA_BIO, currentBio);
            intent.putExtra(EditProfileActivity.EXTRA_COURSE, currentCourse);
            intent.putExtra(EditProfileActivity.EXTRA_YEAR, currentYear);
            editProfileLauncher.launch(intent);
        });

        // Share Profile click
        binding.btnShareProfile.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out " + currentName + " (@" + currentUsername + ") on Viragram — " + currentUniversity + "'s student social network.");
            startActivity(Intent.createChooser(shareIntent, "Share profile via"));
        });
    }

    private void loadFirestoreProfile() {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        Log.d(TAG, "Loading profile for UID: " + uid);

        if (profileListener != null) {
            profileListener.remove();
        }

        profileListener = FirestoreManager.getInstance().getUserProfileListener(uid, (doc, error) -> {
            if (error != null) {
                Log.e(TAG, "Error loading Firestore profile: " + error.getMessage());
                return;
            }

            if (doc != null && doc.exists()) {
                String name = doc.getString("fullName");
                String username = doc.getString("username");
                String bio = doc.getString("bio");
                String course = doc.getString("course");
                String year = doc.getString("year");
                String uni = doc.getString("university");
                String avatarUrl = doc.getString("avatarUrl");
                
                Long fCount = doc.getLong("followerCount");
                Long followingC = doc.getLong("followingCount");
                Long pCount = doc.getLong("postCount");

                if (name != null) currentName = name;
                if (username != null) currentUsername = username;
                if (bio != null) currentBio = bio;
                if (course != null) currentCourse = course;
                if (year != null) currentYear = year;
                if (uni != null) currentUniversity = uni;
                if (avatarUrl != null) currentAvatarUrl = avatarUrl;
                
                if (fCount != null) followerCount = fCount;
                if (followingC != null) followingCount = followingC;
                if (pCount != null) postCount = pCount;
                
                Log.d(TAG, "Loaded profile: " + currentUsername + " | Posts: " + postCount + " | Followers: " + followerCount);

                updateProfileUi();
            } else {
                Log.d(TAG, "Profile document does not exist for UID: " + uid);
            }
        });
    }

    private void updateProfileUi() {
        if (binding == null) return;

        binding.tvProfileName.setText(currentName.isEmpty() ? "Campus Student" : currentName);
        binding.tvProfileUsername.setText("@" + (currentUsername.isEmpty() ? "student" : currentUsername.replace("@", "")));
        
        String academicInfo = currentCourse;
        if (currentYear != null && !currentYear.isEmpty()) academicInfo += " • " + currentYear;
        if (currentUniversity != null && !currentUniversity.isEmpty()) academicInfo += " • " + currentUniversity;
        binding.tvProfileCourse.setText(academicInfo);
        
        if (currentBio == null || currentBio.isEmpty()) {
            binding.tvProfileBio.setVisibility(View.GONE);
        } else {
            binding.tvProfileBio.setVisibility(View.VISIBLE);
            binding.tvProfileBio.setText(currentBio);
        }

        binding.tvStatPostsCount.setText(String.valueOf(postCount));
        binding.tvStatFollowersCount.setText(String.valueOf(followerCount));
        binding.tvStatFollowingCount.setText(String.valueOf(followingCount));

        if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty() && getContext() != null) {
            Glide.with(this)
                    .load(currentAvatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_1)
                    .error(R.drawable.ic_avatar_1)
                    .into(binding.ivProfileAvatar);
        } else {
            binding.ivProfileAvatar.setImageResource(R.drawable.ic_avatar_1);
        }
    }

    private void setupGridRecyclerView() {
        gridAdapter = new ProfilePostAdapter(post -> {
            Intent intent = new Intent(getContext(), PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getPostId());
            intent.putExtra(PostDetailActivity.EXTRA_AUTHOR_NAME, currentName);
            intent.putExtra(PostDetailActivity.EXTRA_AUTHOR_COURSE, currentCourse + " • " + currentYear);
            
            intent.putExtra(PostDetailActivity.EXTRA_AUTHOR_AVATAR_URL, currentAvatarUrl);
            intent.putExtra(PostDetailActivity.EXTRA_IMAGE_URL, post.getImageUriString());
            
            intent.putExtra(PostDetailActivity.EXTRA_AVATAR_RES, R.drawable.ic_avatar_1);
            intent.putExtra(PostDetailActivity.EXTRA_IMAGE_RES, post.getPostImageResId());
            intent.putExtra(PostDetailActivity.EXTRA_CAPTION, post.getCaption());
            intent.putExtra(PostDetailActivity.EXTRA_LIKES, post.getLikeCount());
            intent.putExtra(PostDetailActivity.EXTRA_COMMENTS, post.getCommentCount());
            intent.putExtra(PostDetailActivity.EXTRA_TIME_AGO, post.getTimeAgo());
            startActivity(intent);
        });

        binding.rvProfileGrid.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.rvProfileGrid.setAdapter(gridAdapter);
    }

    private void setupTabs() {
        binding.btnTabPosts.setOnClickListener(v -> {
            selectTab(true, false, false);
            binding.rvProfileGrid.setVisibility(View.VISIBLE);
            binding.layoutTabEmptyState.setVisibility(View.GONE);
        });

        binding.btnTabSaved.setOnClickListener(v -> {
            selectTab(false, true, false);
            binding.rvProfileGrid.setVisibility(View.GONE);
            binding.layoutTabEmptyState.setVisibility(View.VISIBLE);
            binding.ivEmptyTabIcon.setImageResource(R.drawable.ic_bookmark_24);
            binding.tvEmptyTabTitle.setText("No saved posts yet");
        });

        binding.btnTabTagged.setOnClickListener(v -> {
            selectTab(false, false, true);
            binding.rvProfileGrid.setVisibility(View.GONE);
            binding.layoutTabEmptyState.setVisibility(View.VISIBLE);
            binding.ivEmptyTabIcon.setImageResource(R.drawable.ic_tag_24);
            binding.tvEmptyTabTitle.setText("No tagged posts yet");
        });
    }

    private void selectTab(boolean isPosts, boolean isSaved, boolean isTagged) {
        if (getContext() == null) return;
        int primaryColor = ContextCompat.getColor(requireContext(), R.color.viragram_purple_primary);
        int secondaryColor = ContextCompat.getColor(requireContext(), R.color.viragram_text_secondary);

        binding.btnTabPosts.setColorFilter(isPosts ? primaryColor : secondaryColor);
        binding.btnTabSaved.setColorFilter(isSaved ? primaryColor : secondaryColor);
        binding.btnTabTagged.setColorFilter(isTagged ? primaryColor : secondaryColor);
    }

    private void loadUserPosts() {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUid = currentUser.getUid();

        if (userPostsListener != null) {
            userPostsListener.remove();
        }

        userPostsListener = FirestoreManager.getInstance().getUserPostsListener(currentUid, (querySnapshots, error) -> {
            List<Post> realUserPosts = new ArrayList<>();

            if (querySnapshots != null) {
                for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                    Post p = doc.toObject(Post.class);
                    if (p != null) {
                        realUserPosts.add(p);
                    }
                }
            }

            // Sort real user posts in memory by createdAt descending
            realUserPosts.sort((p1, p2) -> {
                if (p1.getCreatedAt() != null && p2.getCreatedAt() != null) {
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                }
                return 0;
            });
            
            // Only use actual Firebase posts, do not pad with fake data
            if (gridAdapter != null) {
                gridAdapter.setPosts(realUserPosts);
            }
            
            // Sync local post count if not yet synced by profile document listener
            if (postCount != realUserPosts.size()) {
                postCount = realUserPosts.size();
                if (binding != null) {
                    binding.tvStatPostsCount.setText(String.valueOf(postCount));
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (profileListener != null) profileListener.remove();
        if (userPostsListener != null) userPostsListener.remove();
        super.onDestroyView();
        binding = null;
    }
}