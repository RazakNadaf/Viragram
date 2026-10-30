package com.example.viragram.utils;

import android.content.Context;

import com.google.firebase.FirebaseApp;

/**
 * Access point and initializer for Firebase services on Viragram.
 */
public class FirebaseConfig {

    private static boolean isInitialized = false;

    public static synchronized void initialize(Context context) {
        if (!isInitialized && context != null) {
            FirebaseApp.initializeApp(context.getApplicationContext());
            isInitialized = true;
        }
    }

    public static boolean isFirebaseAvailable() {
        try {
            return FirebaseApp.getInstance() != null;
        } catch (Exception e) {
            return false;
        }
    }
}