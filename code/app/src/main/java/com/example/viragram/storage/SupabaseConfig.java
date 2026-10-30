package com.example.viragram.storage;

import com.example.viragram.BuildConfig;

/**
 * Central configuration class for Viragram Supabase Media Storage.
 */
public class SupabaseConfig {

    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_PUBLISHABLE_KEY = BuildConfig.SUPABASE_PUBLISHABLE_KEY;
    public static final String STORAGE_BUCKET = "viragram-media";

    public static String getPublicUrl(String path) {
        if (path == null || path.isEmpty()) return "";
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        return SUPABASE_URL + "/storage/v1/object/public/" + STORAGE_BUCKET + "/" + path;
    }
}