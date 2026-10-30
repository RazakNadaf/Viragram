package com.example.viragram.models;

/**
 * Model class representing a direct message conversation on Viragram.
 */
public class Conversation {

    private String id;
    private String participantName;
    private String username;
    private int avatarResId;
    private String lastMessage;
    private String timestamp;
    private int unreadCount;
    private boolean isOnline;
    private String statusText;
    private String relatedStudentId;

    public Conversation() {
    }

    public Conversation(String id, String participantName, String username, int avatarResId, String lastMessage, String timestamp, int unreadCount, boolean isOnline, String statusText) {
        this.id = id;
        this.participantName = participantName;
        this.username = username;
        this.avatarResId = avatarResId;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
        this.isOnline = isOnline;
        this.statusText = statusText;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public void setAvatarResId(int avatarResId) {
        this.avatarResId = avatarResId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getRelatedStudentId() {
        return relatedStudentId;
    }

    public void setRelatedStudentId(String relatedStudentId) {
        this.relatedStudentId = relatedStudentId;
    }
}