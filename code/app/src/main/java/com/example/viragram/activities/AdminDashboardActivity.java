package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.viragram.databinding.ActivityAdminDashboardBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.utils.AnimationUtils;
import com.google.firebase.auth.FirebaseUser;

/**
 * Main dashboard for Viragram College Administrators and authorized Club Co-ordinators with ViragramAuth logging.
 */
public class AdminDashboardActivity extends BaseActivity {

    private static final String TAG = "ViragramAuth";

    private ActivityAdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnAdminBack.setOnClickListener(v -> finish());
        
        AnimationUtils.applyStaggeredFadeIn(binding.cardStatStudents, 0);
        AnimationUtils.applyStaggeredFadeIn(binding.cardStatEvents, 1);
        AnimationUtils.applyStaggeredFadeIn(binding.cardStatClubs, 2);
        AnimationUtils.applyStaggeredFadeIn(binding.cardStatAnnouncements, 3);

        verifyAdminAccessAndLoad();
    }

    private void verifyAdminAccessAndLoad() {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "[ViragramAuth] AdminDashboardActivity access denied: currentUser is NULL!");
            Toast.makeText(this, "Access Denied: Please sign in to access Admin Portal.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();
        Log.d(TAG, "[ViragramAuth] AdminDashboardActivity loaded for UID: " + uid);

        // Verify role in Firestore
        FirestoreManager.getInstance().getUserProfile(uid, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                Log.d(TAG, "[ViragramAuth] Firestore role for UID " + uid + " = " + role);

                if (role == null || (!role.equalsIgnoreCase("college_admin") && !role.equalsIgnoreCase("club_admin"))) {
                    Log.w(TAG, "[ViragramAuth] AdminDashboardActivity ACCESS DENIED for UID: " + uid);
                    Toast.makeText(AdminDashboardActivity.this, "Access Denied: College Administrator privileges required.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                Log.d(TAG, "[ViragramAuth] AdminDashboardActivity ACCESS GRANTED for UID: " + uid);
            }
            setupActions();
            loadDashboardCounts();
        });
    }

    private void loadDashboardCounts() {
        FirestoreManager.getInstance().getFirestore().collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                binding.tvAdminStatStudents.setText(String.valueOf(task.getResult().size()));
            }
        });

        FirestoreManager.getInstance().getFirestore().collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                binding.tvAdminStatEvents.setText(String.valueOf(task.getResult().size()));
            }
        });

        FirestoreManager.getInstance().getFirestore().collection("clubs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                binding.tvAdminStatClubs.setText(String.valueOf(task.getResult().size()));
            }
        });

        FirestoreManager.getInstance().getFirestore().collection("announcements").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                binding.tvAdminStatAnnouncements.setText(String.valueOf(task.getResult().size()));
            }
        });
    }

    private void setupActions() {
        binding.btnAdminCreateEvent.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            startActivity(new Intent(this, AdminCreateEventActivity.class));
        });
        
        binding.btnAdminCreateAnnouncement.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            startActivity(new Intent(this, AdminCreateAnnouncementActivity.class));
        });
        
        binding.btnAdminCreateClub.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            startActivity(new Intent(this, AdminCreateClubActivity.class));
        });

        binding.btnAdminManageEvents.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            startActivity(new Intent(this, AdminEventsActivity.class));
        });
        
        binding.btnAdminManageAnnouncements.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            showToast("Manage Notices Portal"); // Placeholder for full Manage Notices list if required later
        });
        
        binding.btnAdminManageClubs.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            startActivity(new Intent(this, AdminClubsActivity.class));
        });
        
        binding.btnAdminManageUsers.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            showToast("Manage Accounts & Roles");
        });
        
        binding.btnAdminManageReports.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            showToast("Moderation & Reports Portal");
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}