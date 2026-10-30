package com.example.viragram.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.viragram.R;
import com.example.viragram.activities.EventDetailActivity;
import com.example.viragram.databinding.ItemTrendingCampusBinding;
import com.example.viragram.models.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Horizontal Adapter for trending campus cards in College Hub.
 */
public class TrendingCampusAdapter extends RecyclerView.Adapter<TrendingCampusAdapter.TrendingViewHolder> {

    private final List<Event> trendingList = new ArrayList<>();

    public void setTrendingItems(List<Event> items) {
        this.trendingList.clear();
        if (items != null) {
            this.trendingList.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTrendingCampusBinding binding = ItemTrendingCampusBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TrendingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendingViewHolder holder, int position) {
        holder.bind(trendingList.get(position));
    }

    @Override
    public int getItemCount() {
        return trendingList.size();
    }

    static class TrendingViewHolder extends RecyclerView.ViewHolder {
        private final ItemTrendingCampusBinding binding;

        public TrendingViewHolder(@NonNull ItemTrendingCampusBinding binding) {
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
                        .into(binding.ivTrendingBanner);
            } else if (event.getImageResId() != 0) {
                binding.ivTrendingBanner.setImageResource(event.getImageResId());
            } else {
                binding.ivTrendingBanner.setImageResource(R.drawable.goa_university_event);
            }

            binding.tvTrendingTitle.setText(event.getTitle());
            binding.tvTrendingSubtitle.setText(event.getLocation() + " • " + event.getDate());

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
    }
}