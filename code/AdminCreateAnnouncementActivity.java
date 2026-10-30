package com.example.viragram.activities;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.databinding.ActivityAdminCreateAnnouncementBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Announcement;
import com.example.viragram.storage.ImageKitStorageManager;
import com.example.viragram.utils.AnimationUtils;
import com.google.firebase.auth.FirebaseUser;

public class AdminCreateAnnouncementActivity extends BaseActivity {

    public static final String EXTRA_ANNOUNCEMENT_ID = "extra_announcement_id";
    private static final String TAG = "ViragramAdminNoticeSave";

    private ActivityAdminCreateAnnouncementBinding binding;
    private Uri selectedImageUri = null;
    private String editAnnouncementId = null;
    private String existingImageUrl = null;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivNoticeCover.setImageURI(uri);
                    binding.layoutImagePlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCreateAnnouncementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editAnnouncementId = getIntent().getStringExtra(EXTRA_ANNOUNCEMENT_ID);
        if (editAnnouncementId != null) {
            binding.toolbar.setTitle("Edit Notice");
            binding.btnSave.setText(R.string.btn_save_changes);
            loadNoticeData(editAnnouncementId);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.layoutImageContainer.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        
        binding.btnSave.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            saveNotice();
        });
    }

    private void loadNoticeData(String noticeId) {
        Log.d(TAG, "Loading notice data for ID: " + noticeId);
        FirestoreManager.getInstance().getFirestore().collection("announcements").document(noticeId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Announcement announcement = task.getResult().toObject(Announcement.class);
                if (announcement != null) {
                    binding.etTitle.setText(announcement.getTitle());
                    binding.etCategory.setText(announcement.getCategory());
                    binding.etDescription.setText(announcement.getDescription());
                    binding.switchPublish.setChecked(announcement.isPublished());
                    
                    if (announcement.getPriority() != null) {
                        if (announcement.getPriority().equalsIgnoreCase("URGENT")) binding.rbUrgent.setChecked(true);
                        else if (announcement.getPriority().equalsIgnoreCase("IMPORTANT")) binding.rbImportant.setChecked(true);
                        else binding.rbNormal.setChecked(true);
                    }
                    
                    if (announcement.getImageUrl() != null && !announcement.getImageUrl().isEmpty()) {
                        existingImageUrl = announcement.getImageUrl();
                        binding.layoutImagePlaceholder.setVisibility(View.GONE);
                        Glide.with(this).load(existingImageUrl).into(binding.ivNoticeCover);
                    }
                }
            } else {
                Log.e(TAG, "Error loading notice data: " + (task.getException() != null ? task.getException().getMessage() : "Unknown"));
            }
        });
    }

    private void saveNotice() {
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String category = binding.etCategory.getText() != null ? binding.etCategory.getText().toString().trim() : "";
        String description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
        boolean published = binding.switchPublish.isChecked();
        
        String p = "NORMAL";
        if (binding.rbUrgent.isChecked()) p = "URGENT";
        else if (binding.rbImportant.isChecked()) p = "IMPORTANT";
        
        final String priority = p;

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please fill in title and description", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);
        binding.pbUploading.setVisibility(View.VISIBLE);

        String noticeId = editAnnouncementId != null ? editAnnouncementId : "notice_" + System.currentTimeMillis();
        Log.d(TAG, "Saving notice... UID: " + currentUser.getUid() + ", NoticeId: " + noticeId);

        if (selectedImageUri != null) {
            Log.d(TAG, "Uploading notice image to ImageKit...");
            ImageKitStorageManager.getInstance().uploadPostImage(this, currentUser.getUid(), selectedImageUri, new ImageKitStorageManager.StorageCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    Log.d(TAG, "ImageKit upload success: " + imageUrl);
                    persistToFirestore(noticeId, title, category, description, priority, imageUrl, published, currentUser.getUid(), currentUser.getDisplayName());
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "ImageKit upload error: " + errorMessage);
                    binding.btnSave.setEnabled(true);
                    binding.pbUploading.setVisibility(View.GONE);
                    Toast.makeText(AdminCreateAnnouncementActivity.this, "Image upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.d(TAG, "No new image selected. Proceeding to Firestore save.");
            persistToFirestore(noticeId, title, category, description, priority, existingImageUrl != null ? existingImageUrl : "", published, currentUser.getUid(), currentUser.getDisplayName());
        }
    }

    private void persistToFirestore(String noticeId, String title, String category, String description, String priority, String imageUrl, boolean published, String adminUid, String authorName) {
        Log.d(TAG, "Persisting notice to Firestore. Path: announcements/" + noticeId);
        
        Announcement announcement = new Announcement();
        announcement.setId(noticeId);
        announcement.setTitle(title);
        announcement.setCategory(category != null && !category.isEmpty() ? category : "GENERAL");
        announcement.setDescription(description);
        announcement.setPriority(priority);
        announcement.setAuthor(authorName != null ? authorName : "Campus Admin");
        announcement.setTimestamp("Just now"); 
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            announcement.setImageUrl(imageUrl);
        }
        announcement.setPublished(published);
        announcement.setCreatedBy(adminUid);

        FirestoreManager.getInstance().createAnnouncement(announcement, task -> {
            binding.btnSave.setEnabled(true);
            binding.pbUploading.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                Log.d(TAG, "Notice saved successfully to Firestore.");
                Toast.makeText(this, "Notice saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e(TAG, "Failed to save notice to Firestore: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                Toast.makeText(this, "Failed to save notice", Toast.LENGTH_SHORT).show();
            }
        });
    }
}