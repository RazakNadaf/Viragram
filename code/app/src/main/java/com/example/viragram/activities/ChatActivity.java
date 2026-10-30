package com.example.viragram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.viragram.R;
import com.example.viragram.adapters.ChatMessageAdapter;
import com.example.viragram.databinding.ActivityChatBinding;
import com.example.viragram.models.ChatMessage;
import com.example.viragram.models.Conversation;
import com.example.viragram.utils.LocalMessageManager;

import java.util.List;

/**
 * Activity for direct messaging chat thread with a student.
 */
public class ChatActivity extends BaseActivity {

    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    public static final String EXTRA_PARTICIPANT_NAME = "extra_participant_name";
    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_AVATAR_RES = "extra_avatar_res";
    public static final String EXTRA_STATUS_TEXT = "extra_status_text";
    public static final String EXTRA_IS_ONLINE = "extra_is_online";

    private ActivityChatBinding binding;
    private ChatMessageAdapter chatAdapter;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnChatBack.setOnClickListener(v -> finish());

        setupChatHeader();
        setupRecyclerView();
        loadMessages();

        binding.btnSendChat.setOnClickListener(v -> performSendMessage());
    }

    private void setupChatHeader() {
        Intent intent = getIntent();
        conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
        String name = intent.getStringExtra(EXTRA_PARTICIPANT_NAME);
        String username = intent.getStringExtra(EXTRA_USERNAME);
        int avatarRes = intent.getIntExtra(EXTRA_AVATAR_RES, R.drawable.ic_avatar_1);
        String statusText = intent.getStringExtra(EXTRA_STATUS_TEXT);
        boolean isOnline = intent.getBooleanExtra(EXTRA_IS_ONLINE, false);

        if (conversationId == null) {
            Conversation conv = LocalMessageManager.getInstance().getOrCreateConversationForStudent(name, username, avatarRes);
            conversationId = conv.getId();
        }

        binding.tvChatHeaderName.setText(name != null ? name : "Campus Student");
        binding.tvChatHeaderStatus.setText(statusText != null ? statusText : (isOnline ? "Online" : "@" + (username != null ? username : "unigoa")));
        binding.ivChatHeaderAvatar.setImageResource(avatarRes != 0 ? avatarRes : R.drawable.ic_avatar_1);

        if (isOnline) {
            binding.vChatHeaderOnline.setVisibility(View.VISIBLE);
        } else {
            binding.vChatHeaderOnline.setVisibility(View.GONE);
        }

        binding.btnChatCall.setOnClickListener(v ->
                Toast.makeText(this, "Calling " + binding.tvChatHeaderName.getText() + "...", Toast.LENGTH_SHORT).show()
        );

        binding.btnChatVideo.setOnClickListener(v ->
                Toast.makeText(this, "Starting video call with " + binding.tvChatHeaderName.getText() + "...", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatMessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvChatMessages.setLayoutManager(layoutManager);
        binding.rvChatMessages.setAdapter(chatAdapter);
    }

    private void loadMessages() {
        if (conversationId == null) return;
        List<ChatMessage> messages = LocalMessageManager.getInstance().getMessages(conversationId);

        if (messages.isEmpty()) {
            binding.tvChatEmptyState.setVisibility(View.VISIBLE);
            binding.rvChatMessages.setVisibility(View.GONE);
        } else {
            binding.tvChatEmptyState.setVisibility(View.GONE);
            binding.rvChatMessages.setVisibility(View.VISIBLE);
            chatAdapter.setMessages(messages);
            binding.rvChatMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    private void performSendMessage() {
        String text = binding.etChatInput.getText() != null ? binding.etChatInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(text)) {
            return;
        }

        LocalMessageManager.getInstance().sendMessage(conversationId, text);
        binding.etChatInput.setText("");

        loadMessages();
    }
}