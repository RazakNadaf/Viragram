package com.example.viragram.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viragram.R;
import com.example.viragram.adapters.ConversationAdapter;
import com.example.viragram.adapters.SearchStudentAdapter;
import com.example.viragram.databinding.ActivityMessagesBinding;
import com.example.viragram.models.Conversation;
import com.example.viragram.models.User;
import com.example.viragram.utils.LocalMessageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Direct messages inbox activity for Viragram campus messaging.
 */
public class MessagesActivity extends BaseActivity {

    private ActivityMessagesBinding binding;
    private ConversationAdapter conversationAdapter;
    private final List<Conversation> allConversations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnMessagesBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        setupSearch();
        setupNewMessageDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(conversation -> {
            LocalMessageManager.getInstance().markConversationAsRead(conversation.getId());
            Intent intent = new Intent(MessagesActivity.this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.getId());
            intent.putExtra(ChatActivity.EXTRA_PARTICIPANT_NAME, conversation.getParticipantName());
            intent.putExtra(ChatActivity.EXTRA_USERNAME, conversation.getUsername());
            intent.putExtra(ChatActivity.EXTRA_AVATAR_RES, conversation.getAvatarResId());
            intent.putExtra(ChatActivity.EXTRA_STATUS_TEXT, conversation.getStatusText());
            intent.putExtra(ChatActivity.EXTRA_IS_ONLINE, conversation.isOnline());
            startActivity(intent);
        });

        binding.rvConversations.setLayoutManager(new LinearLayoutManager(this));
        binding.rvConversations.setAdapter(conversationAdapter);
    }

    private void loadConversations() {
        allConversations.clear();
        allConversations.addAll(LocalMessageManager.getInstance().getConversations());

        if (allConversations.isEmpty()) {
            binding.rvConversations.setVisibility(View.GONE);
            binding.layoutMessagesEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.rvConversations.setVisibility(View.VISIBLE);
            binding.layoutMessagesEmptyState.setVisibility(View.GONE);
            conversationAdapter.setConversations(allConversations);
        }
    }

    private void setupSearch() {
        binding.etSearchMessages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString().trim().toLowerCase() : "";
                if (!query.isEmpty()) {
                    binding.btnClearMessagesSearch.setVisibility(View.VISIBLE);
                    filterConversations(query);
                } else {
                    binding.btnClearMessagesSearch.setVisibility(View.GONE);
                    conversationAdapter.setConversations(allConversations);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.btnClearMessagesSearch.setOnClickListener(v -> {
            binding.etSearchMessages.setText("");
            conversationAdapter.setConversations(allConversations);
        });
    }

    private void filterConversations(String query) {
        List<Conversation> filtered = new ArrayList<>();
        for (Conversation c : allConversations) {
            if (c.getParticipantName().toLowerCase().contains(query)
                    || c.getUsername().toLowerCase().contains(query)
                    || c.getLastMessage().toLowerCase().contains(query)) {
                filtered.add(c);
            }
        }
        conversationAdapter.setConversations(filtered);
    }

    private void setupNewMessageDialog() {
        binding.btnNewMessage.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_new_message, null);

            RecyclerView rvPicker = view.findViewById(R.id.rv_picker_students);
            rvPicker.setLayoutManager(new LinearLayoutManager(this));

            List<User> students = new ArrayList<>();
            students.add(new User("u1", "rahul.patil", "Rahul Patil", "rahul@unigoa.ac.in", String.valueOf(R.drawable.ic_avatar_1), "MCA • 1st Year", "Goa University"));
            students.add(new User("u2", "noor.f", "Noor Fernandes", "noor@unigoa.ac.in", String.valueOf(R.drawable.ic_avatar_2), "MSc Computer Science • 2nd Year", "Goa University"));
            students.add(new User("u3", "arjun.n", "Arjun Naik", "arjun@unigoa.ac.in", String.valueOf(R.drawable.ic_avatar_3), "B.Tech CSE • Final Year", "Goa University"));
            students.add(new User("u4", "ananya.s", "Ananya Sawant", "ananya@unigoa.ac.in", String.valueOf(R.drawable.ic_avatar_6), "BCA • 2nd Year", "Goa University"));

            SearchStudentAdapter pickerAdapter = new SearchStudentAdapter();
            rvPicker.setAdapter(pickerAdapter);
            pickerAdapter.setStudents(students);

            dialog.setContentView(view);
            dialog.show();
        });
    }
}