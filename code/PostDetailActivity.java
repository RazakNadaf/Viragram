package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.databinding.ActivityPostDetailBinding;
import com.example.viragram.databinding.ItemPostBinding;

/**
 * Activity for displaying a full single post detail when tapped from profile grid.
 */
public class PostDetailActivity extends BaseActivity {

    public static final String EXTRA_POST_ID = "extra_post_id";
    public static final String EXTRA_AUTHOR_NAME = "extra_author_name";
    public static final String EXTRA_AUTHOR_COURSE = "extra_author_course";
    public static final String EXTRA_AUTHOR_AVATAR_URL = "extra_author_avatar_url";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_AVATAR_RES = "extra_avatar_res";
    public static final String EXTRA_IMAGE_RES = "extra_image_res";
    public static final String EXTRA_CAPTION = "extra_caption";
    public static final String EXTRA_LIKES = "extra_likes";
    public static final String EXTRA_COMMENTS = "extra_comments";
    public static final String EXTRA_TIME_AGO = "extra_time_ago";

    private ActivityPostDetailBinding binding;
    private boolean isLiked = false;
    private boolean isBookmarked = false;
    private int likeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnDetailBack.setOnClickListener(v -> finish());

        populatePostDetails();
    }

    private void populatePostDetails() {
        Intent intent = getIntent();
        String postId = intent.getStringExtra(EXTRA_POST_ID);
        String authorName = intent.getStringExtra(EXTRA_AUTHOR_NAME);
        String authorCourse = intent.getStringExtra(EXTRA_AUTHOR_COURSE);
        
        String authorAvatarUrl = intent.getStringExtra(EXTRA_AUTHOR_AVATAR_URL);
        String imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);
        
        int avatarRes = intent.getIntExtra(EXTRA_AVATAR_RES, R.drawable.ic_avatar_2);
        int imageRes = intent.getIntExtra(EXTRA_IMAGE_RES, R.drawable.bg_post_fest);
        
        String caption = intent.getStringExtra(EXTRA_CAPTION);
        likeCount = intent.getIntExtra(EXTRA_LIKES, 125);
        int commentCount = intent.getIntExtra(EXTRA_COMMENTS, 18);
        String timeAgo = intent.getStringExtra(EXTRA_TIME_AGO);

        ItemPostBinding postBinding = binding.layoutPostContent;

        String author = authorName != null ? authorName : "Campus Student";

        // Handle Avatar Load
        if (authorAvatarUrl != null && !authorAvatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(authorAvatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_2)
                    .error(R.drawable.ic_avatar_2)
                    .into(postBinding.ivPostAuthorAvatar);
        } else {
            postBinding.ivPostAuthorAvatar.setImageResource(avatarRes);
        }
        
        postBinding.tvAuthorName.setText(author);
        postBinding.tvAuthorCourse.setText(authorCourse != null ? authorCourse : "Goa University Student");
        
        // Ensure image view is fully visible and not zero-sized
        postBinding.ivPostImage.setVisibility(View.VISIBLE);

        // Handle Main Post Image Load
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("ViragramImageLoad", "Post ID: " + postId + " | ImageUrl exists: YES | Starting Glide load");
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.bg_post_fest)
                    .error(R.drawable.bg_post_fest)
                    .into(postBinding.ivPostImage);
        } else {
            Log.d("ViragramImageLoad", "Post ID: " + postId + " | ImageUrl is empty | Falling back to local resource");
            postBinding.ivPostImage.setImageResource(imageRes);
        }
        
        postBinding.tvTimeAgo.setText(timeAgo != null ? timeAgo : "Just now");

        // Caption formatting
        String cap = caption != null ? caption : "";
        SpannableStringBuilder captionBuilder = new SpannableStringBuilder(author + "  " + cap);
        captionBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        postBinding.tvCaption.setText(captionBuilder);

        postBinding.tvLikeCount.setText(likeCount + " likes");
        postBinding.tvViewComments.setText("View all " + commentCount + " comments");

        // Like click
        postBinding.btnLike.setOnClickListener(v -> {
            isLiked = !isLiked;
            likeCount = isLiked ? likeCount + 1 : Math.max(0, likeCount - 1);
            postBinding.tvLikeCount.setText(likeCount + " likes");

            postBinding.btnLike.setImageResource(isLiked ? R.drawable.ic_like_filled_24 : R.drawable.ic_like_outline_24);
            postBinding.btnLike.animate().scaleX(1.25f).scaleY(1.25f).setDuration(100).withEndAction(() ->
                    postBinding.btnLike.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
            ).start();
        });

        // Comment click
        postBinding.btnComment.setOnClickListener(v -> {
            Intent commentIntent = new Intent(this, CommentsActivity.class);
            commentIntent.putExtra(CommentsActivity.EXTRA_POST_ID, intent.getStringExtra(EXTRA_POST_ID));
            commentIntent.putExtra(CommentsActivity.EXTRA_AUTHOR_NAME, author);
            startActivity(commentIntent);
        });

        // Share click
        postBinding.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, author + " on Viragram: " + cap);
            startActivity(Intent.createChooser(shareIntent, "Share post via"));
        });

        // Bookmark click
        postBinding.btnBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            Toast.makeText(this, isBookmarked ? "Post saved to your bookmarks" : "Post removed from bookmarks", Toast.LENGTH_SHORT).show();
        });
    }
}