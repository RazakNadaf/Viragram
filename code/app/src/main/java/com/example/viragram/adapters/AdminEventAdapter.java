package com.example.viragram.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.viragram.databinding.ItemAdminEventBinding;
import com.example.viragram.models.Event;

import java.util.ArrayList;
import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder> {

    private final List<Event> eventList = new ArrayList<>();
    private final OnEventActionListener listener;

    public interface OnEventActionListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    public AdminEventAdapter(OnEventActionListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.eventList.clear();
        if (events != null) {
            this.eventList.addAll(events);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminEventBinding binding = ItemAdminEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminEventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEventViewHolder holder, int position) {
        holder.bind(eventList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class AdminEventViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminEventBinding binding;

        public AdminEventViewHolder(ItemAdminEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Event event, OnEventActionListener listener) {
            Context context = itemView.getContext();
            binding.tvEventTitle.setText(event.getTitle());
            binding.tvEventDate.setText(event.getDate() + " • " + event.getTime());
            binding.tvEventLocation.setText(event.getLocation());
            
            if (event.getImageResId() != 0) {
                binding.ivEventImage.setImageResource(event.getImageResId());
            }

            binding.btnEditEvent.setOnClickListener(v -> listener.onEdit(event));
            binding.btnDeleteEvent.setOnClickListener(v -> listener.onDelete(event));
        }
    }
}