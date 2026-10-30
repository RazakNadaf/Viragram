package com.example.viragram.models;

/**
 * Model class representing a notification entry on Viragram.
 */
public class NotificationItem {

    public enum NotificationType {
        LIKE,
        COMMENT,
        FOLLOW,
        CLUB,
        EVENT
    }

    private String id;
    private NotificationType type;
    private String actorName;
    private String actorUsername;
    private String message;
    private String timeAgo;
    private int actorAvatarResId;
    private int postThumbnailResId;
    private String relatedPostId;
    private String relatedEventId;
    private String relatedClubId;
    private boolean isRead;
    private String sectionHeader;

    public NotificationItem() {
    }

    public NotificationItem(String id, NotificationType type, String actorName, String actorUsername, String message, String timeAgo, int actorAvatarResId, int postThumbnailResId, String sectionHeader) {
        this.id = id;
        this.type = type;
        this.actorName = actorName;
        this.actorUsername = actorUsername;
        this.message = message;
        this.timeAgo = timeAgo;
        this.actorAvatarResId = actorAvatarResId;
        this.postThumbnailResId = postThumbnailResId;
        this.sectionHeader = sectionHeader;
        this.isRead = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public int getActorAvatarResId() {
        return actorAvatarResId;
    }

    public void setActorAvatarResId(int actorAvatarResId) {
        this.actorAvatarResId = actorAvatarResId;
    }

    public int getPostThumbnailResId() {
        return postThumbnailResId;
    }

    public void setPostThumbnailResId(int postThumbnailResId) {
        this.postThumbnailResId = postThumbnailResId;
    }

    public String getRelatedPostId() {
        return relatedPostId;
    }

    public void setRelatedPostId(String relatedPostId) {
        this.relatedPostId = relatedPostId;
    }

    public String getRelatedEventId() {
        return relatedEventId;
    }

    public void setRelatedEventId(String relatedEventId) {
        this.relatedEventId = relatedEventId;
    }

    public String getRelatedClubId() {
        return relatedClubId;
    }

    public void setRelatedClubId(String relatedClubId) {
        this.relatedClubId = relatedClubId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getSectionHeader() {
        return sectionHeader;
    }

    public void setSectionHeader(String sectionHeader) {
        this.sectionHeader = sectionHeader;
    }
}