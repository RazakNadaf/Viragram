package com.example.viragram.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Helper utility for UI interactions and validation in Viragram.
 */
public class UiUtils {

    public static final String REQUIRED_COLLEGE_DOMAIN = "@unigoa.ac.in";

    public static void showToast(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates whether the given email belongs to Goa University (@unigoa.ac.in).
     *
     * @param email The email string to validate
     * @return true if valid Goa University email, false otherwise
     */
    public static boolean isValidCollegeEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        String trimmedLower = email.trim().toLowerCase();
        return trimmedLower.endsWith(REQUIRED_COLLEGE_DOMAIN)
                && trimmedLower.length() > REQUIRED_COLLEGE_DOMAIN.length();
    }

    /**
     * Validates email for login (allows any valid email format for authorized accounts or college domain).
     */
    public static boolean isValidLoginEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        String trimmedLower = email.trim().toLowerCase();
        if (trimmedLower.contains("@") && trimmedLower.contains(".")) {
            return true;
        }
        return isValidCollegeEmail(email);
    }

    private UiUtils() {
        // Private constructor
    }
}