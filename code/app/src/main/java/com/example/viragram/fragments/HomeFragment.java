package com.example.viragram.fragments;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.adapters.PostAdapter;
import com.example.viragram.adapters.StoryAdapter;
import com.example.viragram.databinding.FragmentHomeBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Post;
import com.example.viragram.models.Story;
import com.example.viragram.storage.ImageKitStorageManager;
import com.example.viragram.utils.LocalFeedManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying the main Viragram social media feed & real-time campus stories.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PostAdapter postAdapter;
    private StoryAdapter storyAdapter;

    private ListenerRegistration postsListener;
    private ListenerRegistration storiesListener;

    private final ActivityResultLauncher<String> storyPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadStoryImage(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerViews();
        setupEmptyStateAction();
        loadStories();
        loadRealtimePosts();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStories();
        loadRealtimePosts();
    }

    private void setupRecyclerViews() {
        storyAdapter = new StoryAdapter(story -> {
            if (story.isUserStory()) {
                storyPickerLauncher.launch("image/*");
            } else {
                showStoryViewerDialog(story);
            }
        });

        postAdapter = new PostAdapter(storyAdapter);
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPosts.setAdapter(postAdapter);
    }

    private void setupEmptyStateAction() {
        binding.btnExploreCampus.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigate(R.id.navigation_search);
            } catch (Exception ignored) {
            }
        });
    }

    private void uploadStoryImage(Uri uri) {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please sign in to post stories", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Posting story...", Toast.LENGTH_SHORT).show();

        String uid = currentUser.getUid();
        ImageKitStorageManager.getInstance().uploadStoryImage(requireContext(), uid, uri, new ImageKitStorageManager.StorageCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                String storyId = "story_" + System.currentTimeMillis();
                Story newStory = new Story();
                newStory.setStoryId(storyId);
                newStory.setAuthorId(uid);
                newStory.setAuthorName(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Campus Student");
                newStory.setImageUrl(imageUrl);

                FirestoreManager.getInstance().createStory(newStory, task -> {
                    Toast.makeText(getContext(), "Story posted successfully!", Toast.LENGTH_SHORT).show();
                    loadStories();
                });
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Story upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStoryViewerDialog(Story story) {
        if (getContext() == null) return;
        Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (story.getImageUrl() != null && !story.getImageUrl().isEmpty()) {
            Glide.with(this).load(story.getImageUrl()).placeholder(R.drawable.bg_post_fest).into(imageView);
        } else {
            imageView.setImageResource(story.getAvatarResId() != 0 ? story.getAvatarResId() : R.drawable.bg_post_fest);
        }

        imageView.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(imageView);
        dialog.show();
    }

    private void loadStories() {
        if (storiesListener != null) {
            storiesListener.remove();
        }

        storiesListener = FirestoreManager.getInstance().getActiveStoriesListener((querySnapshots, error) -> {
            List<Story> stories = new ArrayList<>();
            // Always include "Your Story" as first item
            stories.add(new Story("s_user", "Your Story", R.drawable.ic_avatar_2, true, false));

            if (querySnapshots != null && !querySnapshots.isEmpty()) {
                for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                    Story s = doc.toObject(Story.class);
                    if (s != null) {
                        stories.add(s);
                    }
                }
            } else {
                // Add demo stories if no active Firestore stories exist
                stories.add(new Story("s2", "College Fest", R.drawable.ic_avatar_2, false, true));
                stories.add(new Story("s3", "MCA Students", R.drawable.ic_avatar_3, false, true));
                stories.add(new Story("s4", "CS Club", R.drawable.ic_avatar_4, false, true));
                stories.add(new Story("s5", "Sports", R.drawable.ic_avatar_5, false, false));
            }

            storyAdapter.setStories(stories);
        });
    }

    private void loadRealtimePosts() {
        if (postsListener != null) {
            postsListener.remove();
        }

        postsListener = FirestoreManager.getInstance().getFeedPostsListener((querySnapshots, error) -> {
            List<Post> firestorePosts = new ArrayList<>();

            if (querySnapshots != null && !querySnapshots.isEmpty()) {
                for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                    Post p = doc.toObject(Post.class);
                    if (p != null) {
                        firestorePosts.add(p);
                    }
                }
            }

            // Combine Firestore posts with LocalFeedManager default posts
            List<Post> allPosts = new ArrayList<>(firestorePosts);
            allPosts.addAll(LocalFeedManager.getInstance().getFeedPosts());

            if (allPosts.isEmpty()) {
                binding.rvPosts.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.rvPosts.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
                postAdapter.setPosts(allPosts);
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (postsListener != null) postsListener.remove();
        if (storiesListener != null) storiesListener.remove();
        super.onDestroyView();
        binding = null;
    }
}