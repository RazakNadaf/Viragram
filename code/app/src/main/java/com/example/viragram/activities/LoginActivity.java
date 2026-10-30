package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.viragram.MainActivity;
import com.example.viragram.R;
import com.example.viragram.databinding.ActivityLoginBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.utils.AnimationUtils;
import com.example.viragram.utils.UiUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

/**
 * Authentication activity for user sign-in on Viragram with real Firebase Authentication & ViragramAuth logging.
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "ViragramAuth";

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AnimationUtils.applySlideUpFadeIn(binding.ivLoginLogo);
        AnimationUtils.applySlideUpFadeIn(binding.tvLoginSubtitle);
        AnimationUtils.applySlideUpFadeIn(binding.cardLoginForm);
        AnimationUtils.applyFadeIn(binding.layoutDivider);
        AnimationUtils.applyFadeIn(binding.btnGoToRegister);

        binding.btnLoginSubmit.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            performLogin();
        });

        binding.btnGoToRegister.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.btnForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuthManager.getInstance().isUserSignedIn()) {
            FirebaseUser user = FirebaseAuthManager.getInstance().getCurrentUser();
            boolean isVerified = FirebaseAuthManager.getInstance().isEmailVerified();
            Log.d(TAG, "[ViragramAuth] Auto-login check - UID: " + (user != null ? user.getUid() : "NULL") + " | isEmailVerified: " + isVerified);
            if (isVerified) {
                checkRoleAndNavigateMain(user != null ? user.getUid() : "");
            }
        }
    }

    private void performLogin() {
        String email = binding.etLoginEmail.getText() != null ? binding.etLoginEmail.getText().toString().trim().toLowerCase() : "";
        String password = binding.etLoginPassword.getText() != null ? binding.etLoginPassword.getText().toString().trim() : "";

        binding.tilLoginEmail.setError(null);
        binding.tilLoginPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            binding.tilLoginEmail.setError(getString(R.string.err_enter_email));
            return;
        }

        if (!UiUtils.isValidLoginEmail(email)) {
            binding.tilLoginEmail.setError(getString(R.string.err_invalid_college_email));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilLoginPassword.setError(getString(R.string.err_enter_password));
            return;
        }

        setLoadingState(true);

        FirebaseAuthManager.getInstance().signInWithEmailAndPassword(email, password, (Task<AuthResult> task) -> {
            if (task.isSuccessful()) {
                FirebaseAuthManager.getInstance().reloadCurrentUser((Task<Void> reloadTask) -> {
                    setLoadingState(false);
                    FirebaseUser user = FirebaseAuthManager.getInstance().getCurrentUser();
                    boolean isVerified = FirebaseAuthManager.getInstance().isEmailVerified();
                    String uid = user != null ? user.getUid() : "NULL";

                    Log.d(TAG, "[ViragramAuth] Login Success - UID: " + uid);
                    Log.d(TAG, "[ViragramAuth] Email: " + email);
                    Log.d(TAG, "[ViragramAuth] isEmailVerified: " + isVerified);

                    if (isVerified) {
                        Toast.makeText(LoginActivity.this, "Welcome to Viragram!", Toast.LENGTH_SHORT).show();
                        checkRoleAndNavigateMain(uid);
                    } else {
                        Toast.makeText(LoginActivity.this, "Please verify your email address before signing in.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, VerifyEmailActivity.class);
                        intent.putExtra(RegisterActivity.EXTRA_REGISTERED_EMAIL, email);
                        startActivity(intent);
                    }
                });
            } else {
                setLoadingState(false);
                String errorMsg = FirebaseAuthManager.getInstance().getFriendlyErrorMessage(task.getException());
                Log.e(TAG, "[ViragramAuth] Login Failed: " + errorMsg);
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkRoleAndNavigateMain(String uid) {
        if (uid.isEmpty()) {
            navigateToMain();
            return;
        }

        FirestoreManager.getInstance().getUserProfile(uid, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                Log.d(TAG, "[ViragramAuth] Firestore role for UID " + uid + " = " + role);
            }
            navigateToMain();
        });
    }

    private void setLoadingState(boolean isLoading) {
        binding.btnLoginSubmit.setEnabled(!isLoading);
        binding.btnLoginSubmit.setText(isLoading ? "Logging in..." : getString(R.string.btn_login));
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}