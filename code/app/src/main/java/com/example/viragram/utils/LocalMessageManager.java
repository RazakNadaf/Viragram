package com.example.viragram.utils;

import com.example.viragram.R;
import com.example.viragram.models.ChatMessage;
import com.example.viragram.models.Conversation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton repository manager for direct messaging on Viragram.
 */
public class LocalMessageManager {

    private static LocalMessageManager instance;
    private final List<Conversation> conversations = new ArrayList<>();
    private final Map<String, List<ChatMessage>> messagesMap = new HashMap<>();

    private LocalMessageManager() {
        initDemoData();
    }

    public static synchronized LocalMessageManager getInstance() {
        if (instance == null) {
            instance = new LocalMessageManager();
        }
        return instance;
    }

    private void initDemoData() {
        conversations.clear();
        messagesMap.clear();

        // 1. Rahul Patil
        Conversation c1 = new Conversation("c1", "Rahul Patil", "rahul.patil", R.drawable.ic_avatar_1, "Are you coming to the event tomorrow?", "2m ago", 1, true, "Online");
        List<ChatMessage> m1 = new ArrayList<>();
        m1.add(new ChatMessage("m1_1", "c1", "Rahul Patil", "Hey! Did you see the fest photos?", "10:30 AM", false));
        m1.add(new ChatMessage("m1_2", "c1", "You", "Yes! They look amazing 🔥", "10:32 AM", true));
        m1.add(new ChatMessage("m1_3", "c1", "Rahul Patil", "Glad you liked them.", "10:35 AM", false));
        m1.add(new ChatMessage("m1_4", "c1", "Rahul Patil", "Are you coming to the event tomorrow?", "10:40 AM", false));
        messagesMap.put("c1", m1);

        // 2. Noor Fernandes
        Conversation c2 = new Conversation("c2", "Noor Fernandes", "noor.f", R.drawable.ic_avatar_2, "Great! Let's form a team for Hackathon 2026.", "18m ago", 2, true, "Active 18m ago");
        List<ChatMessage> m2 = new ArrayList<>();
        m2.add(new ChatMessage("m2_1", "c2", "Noor Fernandes", "Are you joining the hackathon?", "10:15 AM", false));
        m2.add(new ChatMessage("m2_2", "c2", "You", "Yes, definitely!", "10:20 AM", true));
        m2.add(new ChatMessage("m2_3", "c2", "Noor Fernandes", "Great! Let's form a team for Hackathon 2026.", "10:22 AM", false));
        messagesMap.put("c2", m2);

        // 3. Arjun Naik
        Conversation c3 = new Conversation("c3", "Arjun Naik", "arjun.n", R.drawable.ic_avatar_3, "See you at the sports meet tomorrow", "1h ago", 0, false, "Active 1h ago");
        List<ChatMessage> m3 = new ArrayList<>();
        m3.add(new ChatMessage("m3_1", "c3", "Arjun Naik", "See you at the sports meet tomorrow", "9:30 AM", false));
        messagesMap.put("c3", m3);

        // 4. Computer Science Club
        Conversation c4 = new Conversation("c4", "Computer Science Club", "cs.club", R.drawable.ic_avatar_4, "Registrations for Hackathon 2026 are now open.", "3h ago", 0, false, "Active 3h ago");
        List<ChatMessage> m4 = new ArrayList<>();
        m4.add(new ChatMessage("m4_1", "c4", "Computer Science Club", "Registrations for Hackathon 2026 are now open.", "8:00 AM", false));
        messagesMap.put("c4", m4);

        // 5. Sports Club GU
        Conversation c5 = new Conversation("c5", "Sports Club GU", "sports.gu", R.drawable.ic_avatar_5, "Football practice starts at 5 PM today.", "Yesterday", 0, false, "Active 1d ago");
        List<ChatMessage> m5 = new ArrayList<>();
        m5.add(new ChatMessage("m5_1", "c5", "Sports Club GU", "Football practice starts at 5 PM today.", "Yesterday", false));
        messagesMap.put("c5", m5);

        conversations.add(c1);
        conversations.add(c2);
        conversations.add(c3);
        conversations.add(c4);
        conversations.add(c5);
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    public List<ChatMessage> getMessages(String conversationId) {
        List<ChatMessage> list = messagesMap.get(conversationId);
        if (list == null) {
            list = new ArrayList<>();
            messagesMap.put(conversationId, list);
        }
        return list;
    }

    public void sendMessage(String conversationId, String messageText) {
        if (conversationId == null || messageText == null || messageText.trim().isEmpty()) return;

        List<ChatMessage> messages = getMessages(conversationId);
        ChatMessage newMsg = new ChatMessage("msg_" + System.currentTimeMillis(), conversationId, "You", messageText.trim(), "Just now", true);
        messages.add(newMsg);

        // Update conversation summary
        for (Conversation c : conversations) {
            if (c.getId().equals(conversationId)) {
                c.setLastMessage("You: " + messageText.trim());
                c.setTimestamp("Just now");
                c.setUnreadCount(0);
                break;
            }
        }
    }

    public void markConversationAsRead(String conversationId) {
        for (Conversation c : conversations) {
            if (c.getId().equals(conversationId)) {
                c.setUnreadCount(0);
                break;
            }
        }
    }

    public Conversation getOrCreateConversationForStudent(String name, String username, int avatarResId) {
        if (name == null) name = "Campus Student";
        if (username == null) username = "student.unigoa";

        for (Conversation c : conversations) {
            if (c.getParticipantName().equalsIgnoreCase(name) || c.getUsername().equalsIgnoreCase(username)) {
                return c;
            }
        }

        String id = "c_student_" + System.currentTimeMillis();
        Conversation newConv = new Conversation(id, name, username, avatarResId != 0 ? avatarResId : R.drawable.ic_avatar_2, "Conversation started", "Just now", 0, true, "Online");
        conversations.add(0, newConv);

        List<ChatMessage> initialList = new ArrayList<>();
        initialList.add(new ChatMessage("m_init_" + id, id, name, "Hi! Connected via Viragram.", "Just now", false));
        messagesMap.put(id, initialList);

        return newConv;
    }
}