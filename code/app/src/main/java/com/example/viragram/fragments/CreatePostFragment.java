package com.example.viragram.fragments;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.viragram.R;
import com.example.viragram.databinding.FragmentCreatePostBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Post;
import com.example.viragram.storage.ImageKitStorageManager;
import com.example.viragram.utils.AnimationUtils;
import com.example.viragram.utils.LocalFeedManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for creating new media posts on Viragram and persisting in Cloud Firestore (`posts/{postId}`).
 */
public class CreatePostFragment extends Fragment {

    private static final String TAG = "ViragramPostFlow";

    private FragmentCreatePostBinding binding;

    private Uri selectedImageUri = null;
    private int selectedDemoResId = 0;
    private String selectedLocation = "Goa University Campus";
    private final List<String> taggedStudents = new ArrayList<>();

    // Upgraded to Modern Photo Picker (PickVisualMedia) to fix API 37 GetContent single-image limitation
    private final ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
                    Log.d(TAG, "Photo Picker Selected URI: " + uri.toString());
                    selectedImageUri = uri;
                    selectedDemoResId = 0;
                    binding.ivCreatePostPreview.setImageURI(uri);
                    showImageSelectedState();
                } else if (selectedDemoResId == 0) {
                    Log.d(TAG, "Photo Picker was canceled.");
                    Toast.makeText(getContext(), "No photo selected", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupImagePickers();
        setupCaptionCounter();
        setupOptionRows();
        setupPostActions();
    }

    private void setupImagePickers() {
        binding.btnSelectGalleryImage.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            launchModernPhotoPicker();
        });

        binding.btnUseDemoImage.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            selectedImageUri = null;
            selectedDemoResId = R.drawable.goa_university_campus;
            binding.ivCreatePostPreview.setImageResource(selectedDemoResId);
            showImageSelectedState();
            Toast.makeText(getContext(), "Demo campus photo selected", Toast.LENGTH_SHORT).show();
        });

        binding.btnChangePhoto.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            launchModernPhotoPicker();
        });
    }

    private void launchModernPhotoPicker() {
        Log.d(TAG, "Launching modern Photo Picker (PickVisualMedia)");
        imagePickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void showImageSelectedState() {
        binding.layoutImageEmptyState.setVisibility(View.GONE);
        AnimationUtils.applyFadeIn(binding.layoutImageSelectedContainer);
    }

    private void setupCaptionCounter() {
        binding.etCreateCaption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s != null ? s.length() : 0;
                binding.tvCaptionCounter.setText(length + "/300");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupOptionRows() {
        binding.btnOptionLocation.setOnClickListener(v -> showLocationDialog());
        binding.btnOptionTagPeople.setOnClickListener(v -> showTagPeopleDialog());
    }

    private void showLocationDialog() {
        if (getContext() == null) return;
        Dialog dialog = new Dialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_select_location, null);

        RadioGroup rg = view.findViewById(R.id.rg_locations);
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = view.findViewById(checkedId);
            if (rb != null) {
                selectedLocation = rb.getText().toString();
                binding.tvSelectedLocation.setText(selectedLocation);
            }
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showTagPeopleDialog() {
        if (getContext() == null) return;
        Dialog dialog = new Dialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_tag_people, null);

        view.findViewById(R.id.btn_done_tagging).setOnClickListener(v -> {
            taggedStudents.clear();
            taggedStudents.add("Rahul Patil");
            binding.tvTaggedPeopleCount.setText("Rahul Patil");
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void setupPostActions() {
        binding.btnCreateClose.setOnClickListener(v -> navigateToHome());

        View.OnClickListener shareClickListener = v -> {
            AnimationUtils.applyPressScale(v);
            attemptPostShare();
        };
        binding.btnCreateShare.setOnClickListener(shareClickListener);
        binding.btnBottomPostSubmit.setOnClickListener(shareClickListener);
    }

    private void attemptPostShare() {
        if (selectedImageUri == null && selectedDemoResId == 0) {
            Snackbar.make(binding.getRoot(), "Please select an image for your post", Snackbar.LENGTH_SHORT).show();
            return;
        }

        binding.pbSharingProgress.setVisibility(View.VISIBLE);
        binding.btnCreateShare.setEnabled(false);
        binding.btnBottomPostSubmit.setEnabled(false);
        binding.btnBottomPostSubmit.setText("Publishing...");

        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            Log.e(TAG, "[ViragramPostFlow] CurrentUser=NULL. Cannot upload post.");
            Toast.makeText(getContext(), "Session expired. Please sign in.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String uid = currentUser.getUid();
        Log.d(TAG, "[ViragramPostFlow] CurrentUser=AUTHENTICATED");
        Log.d(TAG, "[ViragramPostFlow] UID=" + uid);

        // Fetch current user details dynamically from Firestore
        FirestoreManager.getInstance().getUserProfile(uid, task -> {
            String authorName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Campus Student";
            String authorUsername = currentUser.getEmail() != null && currentUser.getEmail().contains("@") ? currentUser.getEmail().split("@")[0] : "student";
            String authorCourse = "Goa University Student";
            String authorAvatarUrl = "";

            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.getString("fullName") != null) authorName = doc.getString("fullName");
                if (doc.getString("username") != null) authorUsername = doc.getString("username");
                if (doc.getString("course") != null) {
                    authorCourse = doc.getString("course") + (doc.getString("year") != null ? " • " + doc.getString("year") : "");
                }
                if (doc.getString("avatarUrl") != null) authorAvatarUrl = doc.getString("avatarUrl");
            }

            final String finalName = authorName;
            final String finalUsername = authorUsername;
            final String finalCourse = authorCourse;
            final String finalAvatarUrl = authorAvatarUrl;

            if (selectedImageUri != null) {
                Log.d(TAG, "[ViragramPostFlow] ImageKit upload started for URI: " + selectedImageUri);
                ImageKitStorageManager.getInstance().uploadPostImage(requireContext(), uid, selectedImageUri, new ImageKitStorageManager.StorageCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        Log.d(TAG, "[ViragramPostFlow] ImageKit upload SUCCESS url=" + imageUrl);
                        savePostToFirestore(uid, finalName, finalUsername, finalCourse, finalAvatarUrl, imageUrl, 0);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "[ViragramPostFlow] ImageKit upload FAILED. Error: " + errorMessage);
                        binding.pbSharingProgress.setVisibility(View.GONE);
                        binding.btnCreateShare.setEnabled(true);
                        binding.btnBottomPostSubmit.setEnabled(true);
                        binding.btnBottomPostSubmit.setText("Share Post");
                        Snackbar.make(binding.getRoot(), "Post image upload failed: " + errorMessage, Snackbar.LENGTH_LONG).show();
                    }
                });
            } else {
                savePostToFirestore(uid, finalName, finalUsername, finalCourse, finalAvatarUrl, "", selectedDemoResId);
            }
        });
    }

    private void savePostToFirestore(String uid, String name, String username, String course, String avatarUrl, String imageUrl, int demoResId) {
        String postId = "post_" + System.currentTimeMillis();
        String caption = binding.etCreateCaption.getText() != null ? binding.etCreateCaption.getText().toString().trim() : "";
        
        Log.d(TAG, "[ViragramPostFlow] Building Firestore post object");

        Post newPost = new Post();
        newPost.setPostId(postId);
        newPost.setAuthorId(uid);
        newPost.setAuthorName(name);
        newPost.setAuthorUsername(username);
        newPost.setAuthorCourseAndYear(course);
        newPost.setAuthorAvatarUrl(avatarUrl);
        newPost.setImageUriString(imageUrl);
        newPost.setPostImageResId(demoResId);
        newPost.setCaption(caption);
        newPost.setLocation(selectedLocation);
        newPost.setLikeCount(0);
        newPost.setCommentCount(0);
        
        Log.d(TAG, "[ViragramPostFlow] Delegating write to FirestoreManager");

        // Save to Firestore
        FirestoreManager.getInstance().createPost(newPost, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "[ViragramPostFlow] Firestore write SUCCESS posts/" + postId);
                
                // Add to local feed manager for immediate UI feedback ONLY AFTER Firestore succeeds
                LocalFeedManager.getInstance().addPostToTop(newPost);
                binding.btnBottomPostSubmit.setText("Published ✓");
                binding.btnBottomPostSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF10B981)); // Green success color
                
                binding.getRoot().postDelayed(() -> navigateToHome(), 600);
            } else {
                Log.e(TAG, "[ViragramPostFlow] Firestore write FAILED");
                if (task.getException() != null) {
                    Log.e(TAG, "[ViragramPostFlow] ExceptionClass=" + task.getException().getClass().getSimpleName());
                    Log.e(TAG, "[ViragramPostFlow] ExceptionMessage=" + task.getException().getMessage());
                }
                
                binding.pbSharingProgress.setVisibility(View.GONE);
                binding.btnCreateShare.setEnabled(true);
                binding.btnBottomPostSubmit.setEnabled(true);
                binding.btnBottomPostSubmit.setText("Share Post");
                Snackbar.make(binding.getRoot(), "Failed to publish post. Please try again.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToHome() {
        try {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}