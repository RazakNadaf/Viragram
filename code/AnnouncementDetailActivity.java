package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;

import com.example.viragram.R;
import com.example.viragram.databinding.ActivityAnnouncementDetailBinding;

/**
 * Activity displaying full details for a campus announcement.
 */
public class AnnouncementDetailActivity extends BaseActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_DESC = "extra_desc";
    public static final String EXTRA_AUTHOR = "extra_author";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_IMAGE_RES = "extra_image_res";

    private ActivityAnnouncementDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnnouncementDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnAnnouncementBack.setOnClickListener(v -> finish());

        setupDetails();
    }

    private void setupDetails() {
        Intent intent = getIntent();
        String title = intent.getStringExtra(EXTRA_TITLE);
        String category = intent.getStringExtra(EXTRA_CATEGORY);
        String desc = intent.getStringExtra(EXTRA_DESC);
        String author = intent.getStringExtra(EXTRA_AUTHOR);
        String time = intent.getStringExtra(EXTRA_TIME);
        int imageRes = intent.getIntExtra(EXTRA_IMAGE_RES, R.drawable.bg_post_campus);

        binding.tvAnnouncementDetailTitle.setText(title != null ? title : "Campus Announcement");
        binding.tvAnnouncementDetailCategory.setText(category != null ? category : "GENERAL");
        binding.tvAnnouncementDetailAuthorTime.setText("Issued by " + (author != null ? author : "University Office") + " • " + (time != null ? time : "Today"));
        binding.tvAnnouncementDetailDesc.setText(desc != null ? desc : "No additional details available.");
        binding.ivAnnouncementBanner.setImageResource(imageRes);
    }
}