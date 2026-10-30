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
import com.example.viragram.databinding.ActivityAdminCreateClubBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Club;
import com.example.viragram.storage.ImageKitStorageManager;
import com.example.viragram.utils.AnimationUtils;
import com.google.firebase.auth.FirebaseUser;

public class AdminCreateClubActivity extends BaseActivity {

    public static final String EXTRA_CLUB_ID = "extra_club_id";
    private static final String TAG = "ViragramAdminClubSave";

    private ActivityAdminCreateClubBinding binding;
    private Uri selectedImageUri = null;
    private String editClubId = null;
    private String existingImageUrl = null;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivClubCover.setImageURI(uri);
                    binding.layoutImagePlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCreateClubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editClubId = getIntent().getStringExtra(EXTRA_CLUB_ID);
        if (editClubId != null) {
            binding.toolbar.setTitle("Edit Club");
            binding.btnSave.setText(R.string.btn_save_changes);
            loadClubData(editClubId);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        binding.layoutImageContainer.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        
        binding.btnSave.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            saveClub();
        });
    }

    private void loadClubData(String clubId) {
        Log.d(TAG, "Loading club data for ID: " + clubId);
        FirestoreManager.getInstance().getFirestore().collection("clubs").document(clubId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Club club = task.getResult().toObject(Club.class);
                if (club != null) {
                    binding.etName.setText(club.getName());
                    binding.etCategory.setText(club.getCategory());
                    binding.etCoordinator.setText(club.getCoordinator());
                    binding.etDescription.setText(club.getDescription());
                    
                    if (club.getImageUrl() != null && !club.getImageUrl().isEmpty()) {
                        existingImageUrl = club.getImageUrl();
                        binding.layoutImagePlaceholder.setVisibility(View.GONE);
                        Glide.with(this).load(existingImageUrl).into(binding.ivClubCover);
                    }
                }
            } else {
                Log.e(TAG, "Error loading club data: " + (task.getException() != null ? task.getException().getMessage() : "Unknown"));
            }
        });
    }

    private void saveClub() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String category = binding.etCategory.getText() != null ? binding.etCategory.getText().toString().trim() : "";
        String coordinator = binding.etCoordinator.getText() != null ? binding.etCoordinator.getText().toString().trim() : "";
        String description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(coordinator) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);
        binding.pbUploading.setVisibility(View.VISIBLE);

        String clubId = editClubId != null ? editClubId : "club_" + System.currentTimeMillis();
        Log.d(TAG, "Saving club... UID: " + currentUser.getUid() + ", ClubId: " + clubId);

        if (selectedImageUri != null) {
            Log.d(TAG, "Uploading club image to ImageKit...");
            ImageKitStorageManager.getInstance().uploadPostImage(this, currentUser.getUid(), selectedImageUri, new ImageKitStorageManager.StorageCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    Log.d(TAG, "ImageKit upload success: " + imageUrl);
                    persistToFirestore(clubId, name, category, coordinator, description, imageUrl, currentUser.getUid());
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "ImageKit upload error: " + errorMessage);
                    binding.btnSave.setEnabled(true);
                    binding.pbUploading.setVisibility(View.GONE);
                    Toast.makeText(AdminCreateClubActivity.this, "Image upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.d(TAG, "No new image selected. Proceeding to Firestore save.");
            persistToFirestore(clubId, name, category, coordinator, description, existingImageUrl != null ? existingImageUrl : "", currentUser.getUid());
        }
    }

    private void persistToFirestore(String clubId, String name, String category, String coordinator, String description, String imageUrl, String adminUid) {
        Log.d(TAG, "Persisting club to Firestore. Path: clubs/" + clubId);
        
        Club club = new Club();
        club.setClubId(clubId);
        club.setName(name);
        club.setCategory(category);
        club.setCoordinator(coordinator);
        club.setDescription(description);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            club.setImageUrl(imageUrl);
        }
        club.setCreatedBy(adminUid);
        club.setStatus("ACTIVE");

        FirestoreManager.getInstance().createClub(club, task -> {
            binding.btnSave.setEnabled(true);
            binding.pbUploading.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                Log.d(TAG, "Club saved successfully to Firestore.");
                Toast.makeText(this, "Club saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e(TAG, "Failed to save club to Firestore: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                Toast.makeText(this, "Failed to save club", Toast.LENGTH_SHORT).show();
            }
        });
    }
}