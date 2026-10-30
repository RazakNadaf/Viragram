package com.example.viragram.firebase;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Clean wrapper manager encapsulating all Firebase Authentication operations for Viragram.
 */
public class FirebaseAuthManager {

    private static final String TAG = "ViragramLoginDebug";
    private static FirebaseAuthManager instance;
    private final FirebaseAuth firebaseAuth;

    private FirebaseAuthManager() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseAuthManager getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthManager();
        }
        return instance;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isUserSignedIn() {
        return getCurrentUser() != null;
    }

    public boolean isEmailVerified() {
        FirebaseUser user = getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    public void getIdToken(boolean forceRefresh, OnCompleteListener<GetTokenResult> listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            user.getIdToken(forceRefresh).addOnCompleteListener(listener);
        }
    }

    public void createUserWithEmailAndPassword(String email, String password, OnCompleteListener<AuthResult> listener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void updateUserProfile(String displayName, OnCompleteListener<Void> listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();
            user.updateProfile(profileUpdates).addOnCompleteListener(listener);
        }
    }

    public void sendEmailVerification(OnCompleteListener<Void> listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(listener);
        }
    }

    public void signInWithEmailAndPassword(String email, String password, OnCompleteListener<AuthResult> listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void sendPasswordResetEmail(String email, OnCompleteListener<Void> listener) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(listener);
    }

    public void reloadCurrentUser(OnCompleteListener<Void> listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(listener);
        }
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public String getFriendlyErrorMessage(Exception exception) {
        if (exception == null) {
            return "An unexpected authentication error occurred. Please try again.";
        }

        Log.e(TAG, "Login exception class = " + exception.getClass().getName());
        Log.e(TAG, "Login exception message = " + exception.getMessage(), exception);
        Log.e(TAG, "FirebaseAuth is initialized = " + (firebaseAuth != null));

        if (exception instanceof FirebaseAuthUserCollisionException) {
            return "An account with this Goa University email already exists. Please log in.";
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            return "Password is too weak. Please use at least 6 characters.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            FirebaseAuthInvalidCredentialsException e = (FirebaseAuthInvalidCredentialsException) exception;
            Log.e(TAG, "Firebase error code = " + e.getErrorCode());
            return "Invalid email or password. Please check and try again.";
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            FirebaseAuthInvalidUserException e = (FirebaseAuthInvalidUserException) exception;
            Log.e(TAG, "Firebase error code = " + e.getErrorCode());
            return "No account was found with this email.";
        } else if (exception instanceof FirebaseNetworkException) {
            return "Unable to connect to Firebase. Please check your internet connection.";
        } else {
            String msg = exception.getMessage();
            if (msg != null && msg.contains("TOO_MANY_ATTEMPTS_TRY_LATER")) {
                return "Too many login attempts. Please try again later.";
            }
            return (msg != null && !msg.isEmpty()) ? msg : "Authentication failed. Please try again.";
        }
    }
}