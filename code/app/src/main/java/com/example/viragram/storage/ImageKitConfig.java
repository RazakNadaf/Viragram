package com.example.viragram.storage;

import com.example.viragram.BuildConfig;

/**
 * Central configuration class for Viragram ImageKit Media Storage.
 */
public class ImageKitConfig {

    public static final String IMAGEKIT_PUBLIC_KEY = BuildConfig.IMAGEKIT_PUBLIC_KEY;
    public static final String IMAGEKIT_URL_ENDPOINT = "https://ik.imagekit.io/gf66rtc00";

    // Remote Express backend server address hosted on Render
    public static final String BACKEND_BASE_URL = "https://viragram-backend.onrender.com";
    public static final String BACKEND_AUTH_ENDPOINT = BACKEND_BASE_URL + "/api/imagekit/auth";

    public static final String IMAGEKIT_UPLOAD_API = "https://upload.imagekit.io/api/v1/files/upload";

    public static String getImageUrl(String path) {
        if (path == null || path.isEmpty()) return "";
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        if (path.startsWith("/")) {
            return IMAGEKIT_URL_ENDPOINT + path;
        }
        return IMAGEKIT_URL_ENDPOINT + "/" + path;
    }
}