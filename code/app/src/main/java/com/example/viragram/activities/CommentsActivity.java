package com.example.viragram.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.viragram.R;
import com.example.viragram.adapters.CommentAdapter;
import com.example.viragram.databinding.ActivityCommentsBinding;
import com.example.viragram.firebase.FirebaseAuthManager;
import com.example.viragram.firebase.FirestoreManager;
import com.example.viragram.models.Comment;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity displaying real-time post comments and posting new comments to Firestore (`posts/{postId}/comments`).
 */
public class CommentsActivity extends BaseActivity {

    private static final String TAG = "ViragramFirestore";

    public static final String EXTRA_POST_ID = "extra_post_id";
    public static final String EXTRA_AUTHOR_NAME = "extra_author_name";

    private ActivityCommentsBinding binding;
    private CommentAdapter commentAdapter;
    private ListenerRegistration commentsListener;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivCommentsBack.setOnClickListener(v -> finish());

        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null || postId.isEmpty()) {
            postId = "post_default";
        }

        commentAdapter = new CommentAdapter();
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentAdapter);

        loadRealtimeComments();

        binding.btnSendComment.setOnClickListener(v -> addComment());
    }

    private void loadRealtimeComments() {
        if (commentsListener != null) {
            commentsListener.remove();
        }

        commentsListener = FirestoreManager.getInstance().getCommentsListener(postId, (querySnapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "[ViragramFirestore] Error listening to comments: " + error.getMessage(), error);
                return;
            }

            List<Comment> comments = new ArrayList<>();
            if (querySnapshots != null && !querySnapshots.isEmpty()) {
                for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                    Comment c = doc.toObject(Comment.class);
                    if (c != null) {
                        comments.add(c);
                    }
                }
            }

            commentAdapter.setComments(comments);
            if (!comments.isEmpty()) {
                binding.rvComments.smoothScrollToPosition(comments.size() - 1);
            }
        });
    }

    private void addComment() {
        String commentText = binding.etCommentInput.getText() != null ? binding.etCommentInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuthManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to post comments", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSendComment.setEnabled(false);

        String uid = currentUser.getUid();
        FirestoreManager.getInstance().getUserProfile(uid, profileTask -> {
            String authorName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Campus Student";
            String authorAvatarUrl = "";

            if (profileTask.isSuccessful() && profileTask.getResult() != null && profileTask.getResult().exists()) {
                DocumentSnapshot doc = profileTask.getResult();
                if (doc.getString("fullName") != null) authorName = doc.getString("fullName");
                if (doc.getString("avatarUrl") != null) authorAvatarUrl = doc.getString("avatarUrl");
            }

            Comment newComment = new Comment();
            newComment.setPostId(postId);
            newComment.setAuthorId(uid);
            newComment.setAuthorName(authorName);
            newComment.setAuthorAvatarUrl(authorAvatarUrl);
            newComment.setAuthorAvatarResId(R.drawable.ic_avatar_2);
            newComment.setCommentText(commentText);

            FirestoreManager.getInstance().addComment(postId, newComment, task -> {
                binding.btnSendComment.setEnabled(true);
                if (task.isSuccessful()) {
                    binding.etCommentInput.setText("");
                    hideKeyboard();
                    Toast.makeText(CommentsActivity.this, "Comment posted!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "[ViragramFirestore] Comment post error: " + (task.getException() != null ? task.getException().getMessage() : "Unknown"));
                    Toast.makeText(CommentsActivity.this, "Failed to post comment. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (commentsListener != null) {
            commentsListener.remove();
        }
        super.onDestroy();
    }
}