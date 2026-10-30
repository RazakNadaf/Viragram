package com.example.viragram.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Singleton manager for handling asynchronous media uploads to ImageKit via Viragram Express Backend authentication.
 */
public class ImageKitStorageManager {

    private static final String TAG = "ImageKitStorage";

    private static ImageKitStorageManager instance;
    private final OkHttpClient httpClient;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface StorageCallback {
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }

    private ImageKitStorageManager() {
        httpClient = new OkHttpClient();
        executorService = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ImageKitStorageManager getInstance() {
        if (instance == null) {
            instance = new ImageKitStorageManager();
        }
        return instance;
    }

    public void uploadProfilePhoto(Context context, String uid, Uri imageUri, StorageCallback callback) {
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
        String folder = "/profiles/" + uid + "/";
        uploadImageToImageKit(context, imageUri, folder, filename, 800, 80, callback);
    }

    public void uploadPostImage(Context context, String uid, Uri imageUri, StorageCallback callback) {
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
        String folder = "/posts/" + uid + "/";
        uploadImageToImageKit(context, imageUri, folder, filename, 1200, 85, callback);
    }

    public void uploadStoryImage(Context context, String uid, Uri imageUri, StorageCallback callback) {
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
        String folder = "/stories/" + uid + "/";
        uploadImageToImageKit(context, imageUri, folder, filename, 1080, 85, callback);
    }

    private void uploadImageToImageKit(Context context, Uri imageUri, String folder, String filename, int maxDim, int quality, StorageCallback callback) {
        executorService.execute(() -> {
            byte[] bytes = compressImageUri(context, imageUri, maxDim, quality);
            if (bytes == null || bytes.length == 0) {
                Log.e(TAG, "Failed to compress or read image bytes from URI: " + imageUri);
                postError(callback, "Failed to read or process selected image.");
                return;
            }

            fetchImageKitAuthAndUpload(folder, filename, bytes, callback);
        });
    }

    private void fetchImageKitAuthAndUpload(String folder, String filename, byte[] bytes, StorageCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "ImageKit upload aborted: FirebaseAuth.getCurrentUser() is null.");
            postError(callback, "Authentication required. Please sign in to upload media.");
            return;
        }

        currentUser.getIdToken(false).addOnCompleteListener((Task<GetTokenResult> task) -> {
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().getToken() == null) {
                Log.e(TAG, "Failed to fetch Firebase ID token for ImageKit auth.");
                postError(callback, "Authentication failed. Unable to verify user identity.");
                return;
            }

            String idToken = task.getResult().getToken();
            Request authRequest = new Request.Builder()
                    .url(ImageKitConfig.BACKEND_AUTH_ENDPOINT)
                    .addHeader("Authorization", "Bearer " + idToken)
                    .get()
                    .build();

            Log.d(TAG, "Fetching ImageKit auth params from backend: " + ImageKitConfig.BACKEND_AUTH_ENDPOINT);

            httpClient.newCall(authRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Backend auth request network failure: " + e.getMessage(), e);
                    postError(callback, "Backend network error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseStr = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful() && !responseStr.isEmpty()) {
                        try {
                            JSONObject json = new JSONObject(responseStr);
                            String token = json.optString("token", "");
                            String signature = json.optString("signature", "");
                            String expire = json.optString("expire", "");
                            String publicKey = json.optString("publicKey", ImageKitConfig.IMAGEKIT_PUBLIC_KEY);

                            if (token.isEmpty() || signature.isEmpty() || expire.isEmpty() || "0".equals(expire)) {
                                Log.e(TAG, "Backend auth returned incomplete parameters: " + responseStr);
                                postError(callback, "Backend returned incomplete ImageKit authentication parameters.");
                                return;
                            }

                            Log.d(TAG, "Received valid ImageKit auth params from backend (Expire: " + expire + ", PubKey prefix: " + (publicKey.length() > 8 ? publicKey.substring(0, 8) : publicKey) + "). Initiating upload...");
                            executeImageKitUpload(folder, filename, bytes, token, signature, expire, publicKey, callback);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse ImageKit auth params JSON: " + e.getMessage(), e);
                            postError(callback, "Failed to parse ImageKit authentication response.");
                        }
                    } else {
                        Log.e(TAG, "Backend auth returned HTTP " + response.code() + ": " + responseStr);
                        postError(callback, "Backend Auth Error (" + response.code() + "): " + responseStr);
                    }
                }
            });
        });
    }

    private void executeImageKitUpload(String folder, String filename, byte[] bytes, String token, String signature, String expire, String publicKey, StorageCallback callback) {
        String base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP);
        String fileData = "data:image/jpeg;base64," + base64Image;

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileData)
                .addFormDataPart("fileName", filename)
                .addFormDataPart("useUniqueFileName", "true")
                .addFormDataPart("folder", folder)
                .addFormDataPart("publicKey", publicKey)
                .addFormDataPart("token", token)
                .addFormDataPart("signature", signature)
                .addFormDataPart("expire", expire);

        RequestBody requestBody = builder.build();

        Request uploadRequest = new Request.Builder()
                .url(ImageKitConfig.IMAGEKIT_UPLOAD_API)
                .post(requestBody)
                .build();

        Log.d(TAG, "Sending multipart upload request to ImageKit API: " + ImageKitConfig.IMAGEKIT_UPLOAD_API + " | Folder: " + folder + " | File size: " + bytes.length + " bytes");

        httpClient.newCall(uploadRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "ImageKit upload network failure: " + e.getMessage(), e);
                postError(callback, "ImageKit upload failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseStr = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful() || response.code() == 200) {
                    try {
                        JSONObject json = new JSONObject(responseStr);
                        String imageUrl = json.optString("url", "");
                        if (!imageUrl.isEmpty()) {
                            Log.d(TAG, "ImageKit Upload SUCCESS! Generated URL: " + imageUrl);
                            postSuccess(callback, imageUrl);
                        } else {
                            Log.e(TAG, "ImageKit response missing 'url' field: " + responseStr);
                            postError(callback, "ImageKit returned empty image URL.");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing ImageKit upload response: " + e.getMessage(), e);
                        postError(callback, "Error parsing ImageKit upload response.");
                    }
                } else {
                    Log.e(TAG, "ImageKit Upload HTTP " + response.code() + " ERROR: " + responseStr);
                    postError(callback, "ImageKit Upload Error (HTTP " + response.code() + "): " + responseStr);
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

    private void postSuccess(StorageCallback callback, String imageUrl) {
        mainHandler.post(() -> {
            if (callback != null) callback.onSuccess(imageUrl);
        });
    }

    private void postError(StorageCallback callback, String errorMessage) {
        mainHandler.post(() -> {
            if (callback != null) callback.onError(errorMessage);
        });
    }
}