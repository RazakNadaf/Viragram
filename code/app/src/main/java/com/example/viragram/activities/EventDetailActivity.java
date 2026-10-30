package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.databinding.ActivityEventDetailBinding;

/**
 * Activity displaying detailed information for an upcoming campus event.
 */
public class EventDetailActivity extends BaseActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DATE = "extra_date";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_DESC = "extra_desc";
    public static final String EXTRA_ORGANIZER = "extra_organizer";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_IMAGE_RES = "extra_image_res";

    private ActivityEventDetailBinding binding;
    private boolean isInterested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnEventBack.setOnClickListener(v -> finish());

        setupEventDetails();
    }

    private void setupEventDetails() {
        Intent intent = getIntent();
        String title = intent.getStringExtra(EXTRA_TITLE);
        String date = intent.getStringExtra(EXTRA_DATE);
        String time = intent.getStringExtra(EXTRA_TIME);
        String location = intent.getStringExtra(EXTRA_LOCATION);
        String desc = intent.getStringExtra(EXTRA_DESC);
        String organizer = intent.getStringExtra(EXTRA_ORGANIZER);
        String imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);
        int imageRes = intent.getIntExtra(EXTRA_IMAGE_RES, R.drawable.goa_university_event);

        binding.tvEventDetailTitle.setText(title != null ? title : "Campus Event");
        binding.tvEventDetailOrganizer.setText("Organized by " + (organizer != null ? organizer : "Goa University"));
        binding.tvEventDetailDatetime.setText((date != null ? date : "Upcoming") + " • " + (time != null ? time : "4:00 PM"));
        binding.tvEventDetailLocation.setText(location != null ? location : "Main Auditorium");
        binding.tvEventDetailDesc.setText(desc != null ? desc : "Join us for an exciting campus event.");

        // Ensure image view is fully visible and not zero-sized
        binding.ivEventDetailBanner.setVisibility(View.VISIBLE);

        // Handle Main Event Image Load
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("ViragramImageLoad", "Event Title: " + title + " | ImageUrl exists: YES | Starting Glide load");
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.goa_university_event)
                    .error(R.drawable.goa_university_event)
                    .into(binding.ivEventDetailBanner);
        } else {
            Log.d("ViragramImageLoad", "Event Title: " + title + " | ImageUrl is empty | Falling back to local resource");
            binding.ivEventDetailBanner.setImageResource(imageRes);
        }

        binding.btnEventDetailInterested.setOnClickListener(v -> {
            isInterested = !isInterested;
            if (isInterested) {
                binding.btnEventDetailInterested.setText("Interested ✓");
                Toast.makeText(this, "Added to your interested events!", Toast.LENGTH_SHORT).show();
            } else {
                binding.btnEventDetailInterested.setText("I'm Interested");
            }
        });
    }
}