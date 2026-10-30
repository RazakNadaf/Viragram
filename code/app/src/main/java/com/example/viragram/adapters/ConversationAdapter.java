package com.example.viragram.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viragram.databinding.ItemConversationBinding;
import com.example.viragram.models.Conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying conversations list in MessagesActivity.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private final List<Conversation> conversationList = new ArrayList<>();
    private final OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(OnConversationClickListener listener) {
        this.listener = listener;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversationList.clear();
        if (conversations != null) {
            this.conversationList.addAll(conversations);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConversationBinding binding = ItemConversationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ConversationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.bind(conversationList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final ItemConversationBinding binding;

        public ConversationViewHolder(@NonNull ItemConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Conversation conversation, OnConversationClickListener listener) {
            binding.ivConversationAvatar.setImageResource(conversation.getAvatarResId());
            binding.tvConversationName.setText(conversation.getParticipantName());
            binding.tvLastMessage.setText(conversation.getLastMessage());
            binding.tvConversationTime.setText(conversation.getTimestamp());

            // Online Dot
            if (conversation.isOnline()) {
                binding.vOnlineDot.setVisibility(View.VISIBLE);
            } else {
                binding.vOnlineDot.setVisibility(View.GONE);
            }

            // Unread Badge & Styling
            if (conversation.getUnreadCount() > 0) {
                binding.tvUnreadBadge.setVisibility(View.VISIBLE);
                binding.tvUnreadBadge.setText(String.valueOf(conversation.getUnreadCount()));
                binding.tvConversationName.setTypeface(null, android.graphics.Typeface.BOLD);
                binding.tvLastMessage.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                binding.tvUnreadBadge.setVisibility(View.GONE);
                binding.tvConversationName.setTypeface(null, android.graphics.Typeface.BOLD);
                binding.tvLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });
        }
    }
}