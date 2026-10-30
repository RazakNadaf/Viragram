package com.example.viragram.models;

/**
 * Model class representing a single chat message inside a conversation.
 */
public class ChatMessage {

    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String message;
    private String timestamp;
    private boolean isMine;
    private boolean isRead;

    public ChatMessage() {
    }

    public ChatMessage(String id, String conversationId, String senderName, String message, String timestamp, boolean isMine) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
        this.isMine = isMine;
        this.isRead = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}