package com.example.viragram.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viragram.activities.AnnouncementDetailActivity;
import com.example.viragram.databinding.ItemAnnouncementBinding;
import com.example.viragram.models.Announcement;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying official campus announcements.
 */
public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    private final List<Announcement> announcementList = new ArrayList<>();

    public void setAnnouncements(List<Announcement> announcements) {
        this.announcementList.clear();
        if (announcements != null) {
            this.announcementList.addAll(announcements);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAnnouncementBinding binding = ItemAnnouncementBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AnnouncementViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        holder.bind(announcementList.get(position));
    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }

    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        private final ItemAnnouncementBinding binding;

        public AnnouncementViewHolder(@NonNull ItemAnnouncementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Announcement item) {
            Context context = itemView.getContext();

            binding.tvAnnouncementCategory.setText(item.getCategory());
            
            if (item.getPriority() != null && item.getPriority().equalsIgnoreCase("URGENT")) {
                binding.tvAnnouncementCategory.setTextColor(android.graphics.Color.RED);
            } else if (item.getPriority() != null && item.getPriority().equalsIgnoreCase("IMPORTANT")) {
                binding.tvAnnouncementCategory.setTextColor(android.graphics.Color.parseColor("#E11D48"));
            } else {
                binding.tvAnnouncementCategory.setTextColor(android.graphics.Color.parseColor("#7C3AED"));
            }

            binding.tvAnnouncementTime.setText(item.getTimestamp());
            binding.tvAnnouncementTitle.setText(item.getTitle());
            binding.tvAnnouncementDesc.setText(item.getDescription());
            binding.tvAnnouncementAuthor.setText("Issued by " + (item.getAuthor() != null ? item.getAuthor() : "Campus Admin"));

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, AnnouncementDetailActivity.class);
                intent.putExtra(AnnouncementDetailActivity.EXTRA_TITLE, item.getTitle());
                intent.putExtra(AnnouncementDetailActivity.EXTRA_CATEGORY, item.getCategory());
                intent.putExtra(AnnouncementDetailActivity.EXTRA_DESC, item.getDescription());
                intent.putExtra(AnnouncementDetailActivity.EXTRA_AUTHOR, item.getAuthor());
                intent.putExtra(AnnouncementDetailActivity.EXTRA_TIME, item.getTimestamp());
                intent.putExtra(AnnouncementDetailActivity.EXTRA_IMAGE_RES, item.getImageResId());
                context.startActivity(intent);
            });
        }
    }
}