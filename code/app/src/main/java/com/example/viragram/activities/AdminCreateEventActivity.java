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
import com.example.viragram.databinding.ActivityAdminCreateEventBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Event;
import com.example.viragram.storage.ImageKitStorageManager;
import com.example.viragram.utils.AnimationUtils;
import com.google.firebase.auth.FirebaseUser;

public class AdminCreateEventActivity extends BaseActivity {

    public static final String EXTRA_EVENT_ID = "extra_event_id";
    private static final String TAG = "ViragramAdminEventSave";

    private ActivityAdminCreateEventBinding binding;
    private Uri selectedImageUri = null;
    private String editEventId = null;
    private String existingImageUrl = null;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivEventCover.setImageURI(uri);
                    binding.layoutImagePlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCreateEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (editEventId != null) {
            binding.toolbar.setTitle("Edit Event");
            binding.btnSave.setText(com.example.viragram.R.string.btn_save_changes);
            loadEventData(editEventId);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.layoutImageContainer.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            imagePickerLauncher.launch("image/*");
        });
        
        binding.btnSave.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            verifyRoleAndSaveEvent();
        });
    }

    private void loadEventData(String eventId) {
        Log.d(TAG, "Loading event data for ID: " + eventId);
        FirestoreManager.getInstance().getFirestore().collection("events").document(eventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Event event = task.getResult().toObject(Event.class);
                if (event != null) {
                    binding.etTitle.setText(event.getTitle());
                    binding.etCategory.setText(event.getCategory());
                    binding.etDate.setText(event.getDate());
                    binding.etTime.setText(event.getTime());
                    binding.etLocation.setText(event.getLocation());
                    binding.etOrganizer.setText(event.getOrganizer());
                    binding.etDescription.setText(event.getDescription());
                    binding.switchPublish.setChecked(event.isPublished());
                    
                    if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                        existingImageUrl = event.getImageUrl();
                        binding.layoutImagePlaceholder.setVisibility(View.GONE);
                        Glide.with(this).load(existingImageUrl).into(binding.ivEventCover);
                    }
                }
            } else {
                Log.e(TAG, "Error loading event data: " + (task.getException() != null ? task.getException().getMessage() : "Unknown"));
            }
        });
    }

    private void verifyRoleAndSaveEvent() {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirestoreManager.getInstance().getUserProfile(currentUser.getUid(), task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                if (role != null && (role.equalsIgnoreCase("college_admin") || role.equalsIgnoreCase("club_admin"))) {
                    saveEvent(currentUser.getUid());
                } else {
                    Toast.makeText(this, "Access Denied: Admin privileges required", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Error validating permissions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveEvent(String currentUid) {
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String category = binding.etCategory.getText() != null ? binding.etCategory.getText().toString().trim() : "";
        String date = binding.etDate.getText() != null ? binding.etDate.getText().toString().trim() : "";
        String time = binding.etTime.getText() != null ? binding.etTime.getText().toString().trim() : "";
        String location = binding.etLocation.getText() != null ? binding.etLocation.getText().toString().trim() : "";
        String organizer = binding.etOrganizer.getText() != null ? binding.etOrganizer.getText().toString().trim() : "";
        String description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
        boolean published = binding.switchPublish.isChecked();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date) || TextUtils.isEmpty(location) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);
        binding.pbUploading.setVisibility(View.VISIBLE);

        String eventId = editEventId != null ? editEventId : "event_" + System.currentTimeMillis();
        Log.d(TAG, "Saving event... UID: " + currentUid + ", EventId: " + eventId);

        if (selectedImageUri != null) {
            Log.d(TAG, "Uploading event image to ImageKit...");
            ImageKitStorageManager.getInstance().uploadPostImage(this, currentUid, selectedImageUri, new ImageKitStorageManager.StorageCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    Log.d(TAG, "ImageKit upload success: " + imageUrl);
                    persistToFirestore(eventId, title, category, date, time, location, organizer, description, imageUrl, published, currentUid);
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "ImageKit upload error: " + errorMessage);
                    binding.btnSave.setEnabled(true);
                    binding.pbUploading.setVisibility(View.GONE);
                    Toast.makeText(AdminCreateEventActivity.this, "Image upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.d(TAG, "No new image selected. Proceeding to Firestore save.");
            persistToFirestore(eventId, title, category, date, time, location, organizer, description, existingImageUrl != null ? existingImageUrl : "", published, currentUid);
        }
    }

    private void persistToFirestore(String eventId, String title, String category, String date, String time, String location, String organizer, String description, String imageUrl, boolean published, String adminUid) {
        Log.d(TAG, "Persisting event to Firestore. Path: events/" + eventId);
        
        Event event = new Event();
        event.setEventId(eventId);
        event.setTitle(title);
        event.setCategory(category);
        event.setDate(date);
        event.setTime(time);
        event.setLocation(location);
        event.setOrganizer(organizer);
        event.setDescription(description);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            event.setImageUrl(imageUrl);
        }
        event.setPublished(published);
        
        if (editEventId != null) {
            // Update flow
            FirestoreManager.getInstance().updateEvent(event, task -> {
                binding.btnSave.setEnabled(true);
                binding.pbUploading.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    Log.d(TAG, "Event updated successfully in Firestore.");
                    Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Failed to update event: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    Toast.makeText(this, "Failed to update event", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create flow
            event.setCreatedBy(adminUid);
            FirestoreManager.getInstance().createEvent(event, task -> {
                binding.btnSave.setEnabled(true);
                binding.pbUploading.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    Log.d(TAG, "Event saved successfully to Firestore.");
                    Toast.makeText(this, "Event saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Failed to save event to Firestore: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    Toast.makeText(this, "Failed to save event", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}