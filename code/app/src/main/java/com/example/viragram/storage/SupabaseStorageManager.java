package com.example.viragram.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Singleton manager for handling asynchronous media uploads to Supabase Storage with Firebase ID token authorization.
 */
public class SupabaseStorageManager {

    private static final String TAG = "SupabaseStorage";

    private static SupabaseStorageManager instance;
    private final OkHttpClient httpClient;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface StorageCallback {
        void onSuccess(String publicUrl);
        void onError(String errorMessage);
    }

    private SupabaseStorageManager() {
        httpClient = new OkHttpClient();
        executorService = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized SupabaseStorageManager getInstance() {
        if (instance == null) {
            instance = new SupabaseStorageManager();
        }
        return instance;
    }

    public void uploadProfilePhoto(Context context, String uid, Uri imageUri, StorageCallback callback) {
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
        String path = "profile/" + uid + "/" + filename;
        Log.d(TAG, "Initiating profile photo upload to path: " + path);
        uploadCompressedImage(context, imageUri, path, 800, 80, callback);
    }

    public void uploadPostImage(Context context, String uid, Uri imageUri, StorageCallback callback) {
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
        String path = "posts/" + uid + "/" + filename;
        Log.d(TAG, "Initiating post image upload to path: " + path);
        uploadCompressedImage(context, imageUri, path, 1200, 85, callback);
    }

    public void uploadStoryImage(Context context, String uid, Uri imageUri, StorageCallback callback) {
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
        String path = "stories/" + uid + "/" + filename;
        Log.d(TAG, "Initiating story image upload to path: " + path);
        uploadCompressedImage(context, imageUri, path, 1080, 85, callback);
    }

    private void uploadCompressedImage(Context context, Uri imageUri, String path, int maxDim, int quality, StorageCallback callback) {
        executorService.execute(() -> {
            byte[] bytes = compressImageUri(context, imageUri, maxDim, quality);
            if (bytes == null || bytes.length == 0) {
                Log.e(TAG, "Failed to compress or read image bytes from URI: " + imageUri);
                postError(callback, "Failed to read or process selected image.");
                return;
            }

            Log.d(TAG, "Image compressed successfully. Size: " + bytes.length + " bytes.");
            uploadRawBytes(path, bytes, callback);
        });
    }

    public void uploadRawBytes(String path, byte[] bytes, StorageCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Upload aborted: FirebaseAuth.getCurrentUser() is null.");
            postError(callback, "Authentication required. Please sign in to upload media.");
            return;
        }

        Log.d(TAG, "Fetching fresh Firebase ID token for UID: " + currentUser.getUid());

        // Retrieve force-refreshed Firebase ID token (true forces token refresh to include custom claims)
        currentUser.getIdToken(true).addOnCompleteListener((Task<GetTokenResult> task) -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String firebaseIdToken = task.getResult().getToken();
                if (firebaseIdToken != null && !firebaseIdToken.isEmpty()) {
                    Log.d(TAG, "Firebase ID token retrieved successfully. Executing HTTP POST upload...");
                    executeHttpUpload(path, bytes, firebaseIdToken, callback);
                } else {
                    Log.e(TAG, "Firebase ID token retrieved was null or empty.");
                    postError(callback, "Failed to retrieve Firebase authentication token. Please re-authenticate.");
                }
            } else {
                Exception exc = task.getException();
                Log.e(TAG, "Failed to fetch Firebase ID token: " + (exc != null ? exc.getMessage() : "Unknown error"));
                postError(callback, "Authentication verification failed. Please sign in again.");
            }
        });
    }

    private void executeHttpUpload(String path, byte[] bytes, String firebaseIdToken, StorageCallback callback) {
        String url = SupabaseConfig.SUPABASE_URL + "/storage/v1/object/" + SupabaseConfig.STORAGE_BUCKET + "/" + path;

        Log.d(TAG, "Uploading to Supabase Storage URL: " + url);

        RequestBody requestBody = RequestBody.create(bytes, MediaType.parse("image/jpeg"));

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("apikey", SupabaseConfig.SUPABASE_PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer " + firebaseIdToken)
                .addHeader("Content-Type", "image/jpeg")
                .addHeader("x-upsert", "true")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network onFailure during Supabase upload: " + e.getMessage(), e);
                postError(callback, "Upload failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBodyStr = "";
                if (response.body() != null) {
                    responseBodyStr = response.body().string();
                }

                if (response.isSuccessful() || response.code() == 200 || response.code() == 201) {
                    String publicUrl = SupabaseConfig.getPublicUrl(path);
                    Log.d(TAG, "Upload HTTP " + response.code() + " SUCCESS. Public URL: " + publicUrl);
                    postSuccess(callback, publicUrl);
                } else {
                    Log.e(TAG, "Upload HTTP " + response.code() + " FAILED. Response body: " + responseBodyStr);
                    postError(callback, "Supabase Storage Error (" + response.code() + "): " + (responseBodyStr.isEmpty() ? response.message() : responseBodyStr));
                }
            }
        });
    }

    public byte[] compressImageUri(Context context, Uri uri, int maxDim, int quality) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) return null;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            is.close();

            int srcWidth = options.outWidth;
            int srcHeight = options.outHeight;
            int sampleSize = 1;

            while (srcWidth / sampleSize > maxDim || srcHeight / sampleSize > maxDim) {
                sampleSize *= 2;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;

            is = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            if (is != null) is.close();

            if (bitmap == null) return null;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            bitmap.recycle();

            return baos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image URI: " + e.getMessage(), e);
            return null;
        }
    }

    private void postSuccess(StorageCallback callback, String publicUrl) {
        mainHandler.post(() -> {
            if (callback != null) callback.onSuccess(publicUrl);
        });
    }

    private void postError(StorageCallback callback, String errorMessage) {
        mainHandler.post(() -> {
            if (callback != null) callback.onError(errorMessage);
        });
    }
}