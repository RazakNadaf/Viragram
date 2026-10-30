package com.example.viragram.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.viragram.R;
import com.example.viragram.databinding.ActivityForgotPasswordBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.utils.UiUtils;
import com.google.android.gms.tasks.Task;

/**
 * Activity for requesting a password reset email via real Firebase Authentication.
 */
public class ForgotPasswordActivity extends BaseActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSendReset.setOnClickListener(v -> sendResetLink());
        binding.btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void sendResetLink() {
        String email = binding.etForgotEmail.getText() != null ? binding.etForgotEmail.getText().toString().trim().toLowerCase() : "";

        binding.tilForgotEmail.setError(null);

        if (TextUtils.isEmpty(email)) {
            binding.tilForgotEmail.setError(getString(R.string.err_enter_email));
            return;
        }

        if (!UiUtils.isValidCollegeEmail(email)) {
            binding.tilForgotEmail.setError(getString(R.string.err_invalid_college_email));
            return;
        }

        binding.btnSendReset.setEnabled(false);

        FirebaseAuthManager.getInstance().sendPasswordResetEmail(email, (Task<Void> task) -> {
            binding.btnSendReset.setEnabled(true);
            // Always display generic friendly message to prevent account enumeration
            Toast.makeText(this, "If an account exists for " + email + ", a password reset link has been sent.", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}