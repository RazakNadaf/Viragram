package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.viragram.R;
import com.example.viragram.databinding.ActivityRegisterBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.User;
import com.example.viragram.utils.AnimationUtils;
import com.example.viragram.utils.UiUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity for registering a new Viragram student account with real Firebase Authentication & Firestore Profile.
 */
public class RegisterActivity extends BaseActivity {

    public static final String EXTRA_REGISTERED_EMAIL = "extra_registered_email";

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AnimationUtils.applySlideUpFadeIn(binding.ivRegisterLogo);
        AnimationUtils.applySlideUpFadeIn(binding.cardRegisterForm);

        binding.ivRegisterBack.setOnClickListener(v -> finish());
        
        binding.btnAlreadyHaveAccount.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            finish();
        });

        binding.btnRegisterSubmit.setOnClickListener(v -> {
            AnimationUtils.applyPressScale(v);
            performRegistration();
        });
    }

    private void performRegistration() {
        String fullName = binding.etRegisterFullName.getText() != null ? binding.etRegisterFullName.getText().toString().trim() : "";
        String studentId = binding.etRegisterStudentId.getText() != null ? binding.etRegisterStudentId.getText().toString().trim() : "";
        String email = binding.etRegisterEmail.getText() != null ? binding.etRegisterEmail.getText().toString().trim().toLowerCase() : "";
        String course = binding.etRegisterCourse.getText() != null ? binding.etRegisterCourse.getText().toString().trim() : "";
        String year = binding.etRegisterYear.getText() != null ? binding.etRegisterYear.getText().toString().trim() : "";
        String password = binding.etRegisterPassword.getText() != null ? binding.etRegisterPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etRegisterConfirmPassword.getText() != null ? binding.etRegisterConfirmPassword.getText().toString().trim() : "";

        binding.tilRegisterFullName.setError(null);
        binding.tilRegisterStudentId.setError(null);
        binding.tilRegisterEmail.setError(null);
        binding.tilRegisterPassword.setError(null);
        binding.tilRegisterConfirmPassword.setError(null);

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(studentId) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(course) || TextUtils.isEmpty(year)) {
            Toast.makeText(this, R.string.err_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!UiUtils.isValidCollegeEmail(email)) {
            binding.tilRegisterEmail.setError(getString(R.string.err_invalid_college_email));
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.tilRegisterPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.tilRegisterConfirmPassword.setError(getString(R.string.err_password_mismatch));
            return;
        }

        setLoadingState(true);

        FirebaseAuthManager.getInstance().createUserWithEmailAndPassword(email, password, (Task<AuthResult> task) -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = FirebaseAuthManager.getInstance().getCurrentUser();
                String uid = firebaseUser != null ? firebaseUser.getUid() : "uid_" + System.currentTimeMillis();
                String derivedUsername = email.contains("@") ? email.split("@")[0].replace(".", "_") : "student.unigoa";

                User newUser = new User(
                        uid,
                        fullName,
                        derivedUsername,
                        email,
                        studentId,
                        course,
                        year,
                        "Goa University",
                        "Goa University Student | Viragram",
                        ""
                );

                // Create Firestore Profile Document at users/{uid}
                FirestoreManager.getInstance().createUserProfile(newUser, (Task<Void> firestoreTask) -> {
                    // Update Auth Profile Display Name
                    FirebaseAuthManager.getInstance().updateUserProfile(fullName, (Task<Void> profileTask) -> {
                        // Send Verification Email
                        FirebaseAuthManager.getInstance().sendEmailVerification((Task<Void> verifyTask) -> {
                            setLoadingState(false);
                            Toast.makeText(RegisterActivity.this, "Account created! Verification link sent to " + email, Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                            intent.putExtra(EXTRA_REGISTERED_EMAIL, email);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        });
                    });
                });
            } else {
                setLoadingState(false);
                String errorMsg = FirebaseAuthManager.getInstance().getFriendlyErrorMessage(task.getException());
                Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        binding.btnRegisterSubmit.setEnabled(!isLoading);
        binding.btnRegisterSubmit.setText(isLoading ? "Creating Account..." : getString(R.string.btn_register));
    }
}