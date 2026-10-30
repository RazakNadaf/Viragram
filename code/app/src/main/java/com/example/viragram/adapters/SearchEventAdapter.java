package com.example.viragram.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.activities.EventDetailActivity;
import com.example.viragram.databinding.ItemSearchEventBinding;
import com.example.viragram.models.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying upcoming campus events in Search & Explore.
 */
public class SearchEventAdapter extends RecyclerView.Adapter<SearchEventAdapter.EventViewHolder> {

    private final List<Event> eventList = new ArrayList<>();

    public void setEvents(List<Event> events) {
        this.eventList.clear();
        if (events != null) {
            this.eventList.addAll(events);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchEventBinding binding = ItemSearchEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(eventList.get(position));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemSearchEventBinding binding;

        public EventViewHolder(@NonNull ItemSearchEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Event event) {
            Context context = itemView.getContext();

            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(event.getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.goa_university_event)
                        .error(R.drawable.goa_university_event)
                        .into(binding.ivEventBanner);
            } else if (event.getImageResId() != 0) {
                binding.ivEventBanner.setImageResource(event.getImageResId());
            } else {
                binding.ivEventBanner.setImageResource(R.drawable.goa_university_event);
            }

            binding.tvEventTitle.setText(event.getTitle());
            binding.tvEventDateLocation.setText(event.getDate() + " • " + event.getLocation());
            binding.tvEventDescription.setText(event.getDescription());
            binding.tvEventOrganizer.setText("Organized by " + event.getOrganizer());

            updateInterestedUi(context, event);

            binding.btnEventInterested.setOnClickListener(v -> {
                event.setInterested(!event.isInterested());
                updateInterestedUi(context, event);
            });

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra(EventDetailActivity.EXTRA_TITLE, event.getTitle());
                intent.putExtra(EventDetailActivity.EXTRA_DATE, event.getDate());
                intent.putExtra(EventDetailActivity.EXTRA_TIME, event.getTime());
                intent.putExtra(EventDetailActivity.EXTRA_LOCATION, event.getLocation());
                intent.putExtra(EventDetailActivity.EXTRA_DESC, event.getDescription());
                intent.putExtra(EventDetailActivity.EXTRA_ORGANIZER, event.getOrganizer());
                intent.putExtra(EventDetailActivity.EXTRA_IMAGE_URL, event.getImageUrl());
                intent.putExtra(EventDetailActivity.EXTRA_IMAGE_RES, event.getImageResId());
                context.startActivity(intent);
            });
        }

        private void updateInterestedUi(Context context, Event event) {
            if (event.isInterested()) {
                binding.btnEventInterested.setText("Interested ✓");
                binding.btnEventInterested.setTextColor(ContextCompat.getColor(context, R.color.viragram_purple_primary));
                binding.btnEventInterested.setStrokeColorResource(R.color.viragram_purple_primary);
            } else {
                binding.btnEventInterested.setText("Interested");
                binding.btnEventInterested.setTextColor(ContextCompat.getColor(context, R.color.viragram_text_secondary));
                binding.btnEventInterested.setStrokeColorResource(R.color.viragram_border);
            }
        }
    }
}