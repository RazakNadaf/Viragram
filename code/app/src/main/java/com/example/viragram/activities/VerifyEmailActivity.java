package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.example.viragram.MainActivity;
import com.example.viragram.databinding.ActivityVerifyEmailBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity for verifying user email link with real Firebase Authentication and ViragramAuth diagnostics.
 */
public class VerifyEmailActivity extends BaseActivity {

    private static final String TAG = "ViragramAuth";

    private ActivityVerifyEmailBinding binding;
    private CountDownTimer resendTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String email = getIntent().getStringExtra(RegisterActivity.EXTRA_REGISTERED_EMAIL);
        if (email == null || email.isEmpty()) {
            FirebaseUser user = FirebaseAuthManager.getInstance().getCurrentUser();
            if (user != null) {
                email = user.getEmail();
            }
        }

        binding.tvUserEmail.setText(email != null ? email : "student@unigoa.ac.in");

        binding.btnVerifySubmit.setOnClickListener(v -> checkVerificationStatus());

        binding.btnResendOtp.setOnClickListener(v -> resendVerificationEmail());

        binding.btnChangeEmail.setOnClickListener(v -> {
            FirebaseAuthManager.getInstance().signOut();
            Intent intent = new Intent(VerifyEmailActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkVerificationStatus() {
        if (!FirebaseAuthManager.getInstance().isUserSignedIn()) {
            Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        binding.btnVerifySubmit.setEnabled(false);
        binding.btnVerifySubmit.setText("Checking Status...");

        FirebaseAuthManager.getInstance().reloadCurrentUser((Task<Void> task) -> {
            binding.btnVerifySubmit.setEnabled(true);
            binding.btnVerifySubmit.setText("I've Verified");

            FirebaseUser user = FirebaseAuthManager.getInstance().getCurrentUser();
            boolean isVerified = FirebaseAuthManager.getInstance().isEmailVerified();
            Log.d(TAG, "[ViragramAuth] Verification Check - UID: " + (user != null ? user.getUid() : "NULL") + " | isEmailVerified: " + isVerified);

            if (isVerified) {
                Toast.makeText(VerifyEmailActivity.this, "Email verified successfully! Welcome to Viragram.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(VerifyEmailActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(VerifyEmailActivity.this, "Email not verified yet. Please click the link sent to " + binding.tvUserEmail.getText(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resendVerificationEmail() {
        if (!FirebaseAuthManager.getInstance().isUserSignedIn()) {
            Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        binding.btnResendOtp.setEnabled(false);

        FirebaseAuthManager.getInstance().sendEmailVerification((Task<Void> task) -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "[ViragramAuth] Verification email resent successfully.");
                Toast.makeText(VerifyEmailActivity.this, "Verification email resent! Check your inbox.", Toast.LENGTH_SHORT).show();
                startResendCooldown();
            } else {
                binding.btnResendOtp.setEnabled(true);
                String errorMsg = FirebaseAuthManager.getInstance().getFriendlyErrorMessage(task.getException());
                Log.e(TAG, "[ViragramAuth] Resend verification email failed: " + errorMsg);
                Toast.makeText(VerifyEmailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startResendCooldown() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        resendTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.btnResendOtp.setText("Resend Link (" + (millisUntilFinished / 1000) + "s)");
            }

            @Override
            public void onFinish() {
                binding.btnResendOtp.setEnabled(true);
                binding.btnResendOtp.setText("Resend Verification Link");
            }
        }.start();
    }

    private void navigateToLogin() {
        FirebaseAuthManager.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        super.onDestroy();
    }
}