package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.viragram.databinding.ActivityProfileSettingsBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity displaying application settings, account management, and Firebase Authentication logout with ViragramAuth logging.
 */
public class ProfileSettingsActivity extends BaseActivity {

    private static final String TAG = "ViragramAuth";

    private ActivityProfileSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSettingsBack.setOnClickListener(v -> finish());

        setupSettingActions();
        checkAdminRoleAndToggleOption();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAdminRoleAndToggleOption();
    }

    private void checkAdminRoleAndToggleOption() {
        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            binding.btnSettingAdminPortal.setVisibility(View.GONE);
            binding.dividerAdminPortal.setVisibility(View.GONE);
            return;
        }

        String uid = currentUser.getUid();
        Log.d(TAG, "[ViragramAuth] Checking admin role for Settings screen, UID: " + uid);

        FirestoreManager.getInstance().getUserProfile(uid, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                Log.d(TAG, "[ViragramAuth] Firestore role for UID " + uid + " = " + role);

                if (role != null && (role.equalsIgnoreCase("college_admin") || role.equalsIgnoreCase("club_admin"))) {
                    Log.d(TAG, "[ViragramAuth] Showing Admin Portal entry in Settings for UID: " + uid);
                    binding.btnSettingAdminPortal.setVisibility(View.VISIBLE);
                    binding.dividerAdminPortal.setVisibility(View.VISIBLE);
                    return;
                }
            }
            binding.btnSettingAdminPortal.setVisibility(View.GONE);
            binding.dividerAdminPortal.setVisibility(View.GONE);
        });
    }

    private void setupSettingActions() {
        // Account Option -> Opens Edit Profile Activity
        binding.btnSettingAccount.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileSettingsActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Admin Portal Entry
        binding.btnSettingAdminPortal.setOnClickListener(v -> {
            Intent adminIntent = new Intent(ProfileSettingsActivity.this, AdminDashboardActivity.class);
            startActivity(adminIntent);
        });

        // Privacy Option
        binding.btnSettingPrivacy.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("Privacy & Security")
                .setMessage("• Account Privacy: Public Campus Profile\n• Domain Security: @unigoa.ac.in Verified\n• Activity Status: Visible to Classmates\n• Direct Messages: Enabled")
                .setPositiveButton("OK", null)
                .show());

        // Notifications Option
        binding.btnSettingNotifications.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("Notification Preferences")
                .setMessage("• Push Notifications: Enabled\n• Campus Event Reminders: Active\n• Direct Message Alerts: Active\n• Likes & Comments: Active")
                .setPositiveButton("OK", null)
                .show());

        // Appearance Option
        binding.btnSettingAppearance.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("Appearance & Theme")
                .setMessage("Viragram uses your Android system DayNight setting.\n\nTo toggle Light or Dark mode, switch your device System Theme in Android Settings.")
                .setPositiveButton("OK", null)
                .show());

        // Help & Support Option
        binding.btnSettingHelp.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("Help & Campus Support")
                .setMessage("Need assistance with your campus account or moderation?\n\nContact Goa University IT Support:\nEmail: support@unigoa.ac.in\nOffice: Library Building, 1st Floor")
                .setPositiveButton("OK", null)
                .show());

        // About Option
        binding.btnSettingAbout.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("About Viragram")
                .setMessage("Viragram v1.0 • Goa University\n\nA modern social media & campus discovery network built exclusively for Goa University students.")
                .setPositiveButton("OK", null)
                .show());

        // Logout Option
        binding.btnSettingLogout.setOnClickListener(v -> confirmLogout());
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of Viragram?")
                .setPositiveButton("Log Out", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        FirebaseAuthManager.getInstance().signOut();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}