package com.example.viragram.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.databinding.ActivityEditProfileBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.storage.ImageKitStorageManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for editing user profile details and uploading profile avatar to ImageKit Media Storage.
 */
public class EditProfileActivity extends BaseActivity {

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_BIO = "extra_bio";
    public static final String EXTRA_COURSE = "extra_course";
    public static final String EXTRA_YEAR = "extra_year";

    private ActivityEditProfileBinding binding;
    private Uri selectedPhotoUri = null;
    private String currentAvatarUrl = "";

    private final ActivityResultLauncher<String> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    binding.ivEditAvatar.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnEditBack.setOnClickListener(v -> finish());

        loadCurrentProfile();

        binding.btnChangePhoto.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));

        binding.btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadCurrentProfile() {
        // Pre-fill from Intent extras
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String username = getIntent().getStringExtra(EXTRA_USERNAME);
        String bio = getIntent().getStringExtra(EXTRA_BIO);
        String course = getIntent().getStringExtra(EXTRA_COURSE);
        String year = getIntent().getStringExtra(EXTRA_YEAR);

        if (name != null) binding.etEditName.setText(name);
        if (username != null) binding.etEditUsername.setText(username);
        if (bio != null) binding.etEditBio.setText(bio);
        if (course != null) binding.etEditCourse.setText(course);
        if (year != null) binding.etEditYear.setText(year);

        // Fetch from Firestore
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirestoreManager.getInstance().getUserProfile(currentUser.getUid(), (Task<DocumentSnapshot> task) -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    DocumentSnapshot doc = task.getResult();
                    String fName = doc.getString("fullName");
                    String uName = doc.getString("username");
                    String cBio = doc.getString("bio");
                    String cCourse = doc.getString("course");
                    String cYear = doc.getString("year");
                    currentAvatarUrl = doc.getString("avatarUrl");

                    if (fName != null) binding.etEditName.setText(fName);
                    if (uName != null) binding.etEditUsername.setText(uName);
                    if (cBio != null) binding.etEditBio.setText(cBio);
                    if (cCourse != null) binding.etEditCourse.setText(cCourse);
                    if (cYear != null) binding.etEditYear.setText(cYear);

                    if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty() && selectedPhotoUri == null) {
                        Glide.with(this)
                                .load(currentAvatarUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_avatar_2)
                                .into(binding.ivEditAvatar);
                    }
                }
            });
        }
    }

    private void saveProfileChanges() {
        String name = binding.etEditName.getText() != null ? binding.etEditName.getText().toString().trim() : "";
        String username = binding.etEditUsername.getText() != null ? binding.etEditUsername.getText().toString().trim() : "";
        String bio = binding.etEditBio.getText() != null ? binding.etEditBio.getText().toString().trim() : "";
        String course = binding.etEditCourse.getText() != null ? binding.etEditCourse.getText().toString().trim() : "";
        String year = binding.etEditYear.getText() != null ? binding.etEditYear.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User session expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnSaveChanges.setEnabled(false);
        binding.btnSaveChanges.setText("Saving...");

        if (selectedPhotoUri != null) {
            // Upload selected avatar photo to ImageKit Storage: /profiles/{uid}/
            ImageKitStorageManager.getInstance().uploadProfilePhoto(this, currentUser.getUid(), selectedPhotoUri, new ImageKitStorageManager.StorageCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    persistProfileToFirestore(currentUser.getUid(), name, username, bio, course, year, imageUrl);
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, "Avatar upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    // Fallback to saving text metadata without failing entire edit
                    persistProfileToFirestore(currentUser.getUid(), name, username, bio, course, year, currentAvatarUrl);
                }
            });
        } else {
            persistProfileToFirestore(currentUser.getUid(), name, username, bio, course, year, currentAvatarUrl);
        }
    }

    private void persistProfileToFirestore(String uid, String name, String username, String bio, String course, String year, String avatarUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", name);
        updates.put("username", username.replace("@", ""));
        updates.put("bio", bio);
        updates.put("course", course);
        updates.put("year", year);
        if (avatarUrl != null) {
            updates.put("avatarUrl", avatarUrl);
        }

        FirestoreManager.getInstance().updateUserProfile(uid, updates, (Task<Void> task) -> {
            binding.btnSaveChanges.setEnabled(true);
            binding.btnSaveChanges.setText("Save Changes");

            if (task.isSuccessful()) {
                FirebaseAuthManager.getInstance().updateUserProfile(name, null);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_NAME, name);
                resultIntent.putExtra(EXTRA_USERNAME, username);
                resultIntent.putExtra(EXTRA_BIO, bio);
                resultIntent.putExtra(EXTRA_COURSE, course);
                resultIntent.putExtra(EXTRA_YEAR, year);
                setResult(RESULT_OK, resultIntent);

                Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, "Failed to update profile in Firestore.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}