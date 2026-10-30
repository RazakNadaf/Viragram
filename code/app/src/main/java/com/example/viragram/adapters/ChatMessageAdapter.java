package com.example.viragram.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viragram.databinding.ItemChatMessageMineBinding;
import com.example.viragram.databinding.ItemChatMessageOtherBinding;
import com.example.viragram.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for rendering incoming and outgoing chat message bubbles.
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MINE = 0;
    private static final int TYPE_OTHER = 1;

    private final List<ChatMessage> messageList = new ArrayList<>();

    public void setMessages(List<ChatMessage> messages) {
        this.messageList.clear();
        if (messages != null) {
            this.messageList.addAll(messages);
        }
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        if (message != null) {
            this.messageList.add(message);
            notifyItemInserted(this.messageList.size() - 1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isMine() ? TYPE_MINE : TYPE_OTHER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MINE) {
            ItemChatMessageMineBinding binding = ItemChatMessageMineBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new MineViewHolder(binding);
        } else {
            ItemChatMessageOtherBinding binding = ItemChatMessageOtherBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new OtherViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);
        if (holder instanceof MineViewHolder) {
            ((MineViewHolder) holder).bind(msg);
        } else if (holder instanceof OtherViewHolder) {
            ((OtherViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MineViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageMineBinding binding;

        public MineViewHolder(@NonNull ItemChatMessageMineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatMessage message) {
            binding.tvChatMessageText.setText(message.getMessage());
            binding.tvChatMessageTime.setText(message.getTimestamp());
        }
    }

    static class OtherViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageOtherBinding binding;

        public OtherViewHolder(@NonNull ItemChatMessageOtherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatMessage message) {
            binding.tvChatMessageText.setText(message.getMessage());
            binding.tvChatMessageTime.setText(message.getTimestamp());
        }
    }
}