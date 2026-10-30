package com.example.viragram.utils;

/**
 * Application-wide constant definitions for Viragram.
 */
public class Constants {

    public static final String PREF_NAME = "viragram_prefs";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_USER_ID = "user_id";

    // Firestore Collections (for future Firebase integration)
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_POSTS = "posts";
    public static final String COLLECTION_COMMENTS = "comments";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}