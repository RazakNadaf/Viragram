package com.example.viragram.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model class representing a campus story highlight on Viragram and persisted in Firestore (`stories/{storyId}`).
 */
public class Story {

    private String storyId;
    private String authorId;
    private String authorName;
    private String authorUsername;
    private String authorAvatarUrl;
    private String imageUrl;
    private String title;
    private int avatarResId;
    private boolean isUserStory;
    private boolean hasUnseenStory;

    @ServerTimestamp
    private Date createdAt;

    private Date expiresAt;

    public Story() {
    }

    public Story(String storyId, String title, int avatarResId, boolean isUserStory, boolean hasUnseenStory) {
        this.storyId = storyId;
        this.title = title;
        this.avatarResId = avatarResId;
        this.isUserStory = isUserStory;
        this.hasUnseenStory = hasUnseenStory;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        if (title != null && !title.isEmpty()) return title;
        if (authorName != null && !authorName.isEmpty()) return authorName.split(" ")[0];
        return "Story";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public void setAvatarResId(int avatarResId) {
        this.avatarResId = avatarResId;
    }

    public boolean isUserStory() {
        return isUserStory;
    }

    public void setUserStory(boolean userStory) {
        isUserStory = userStory;
    }

    public boolean hasUnseenStory() {
        return hasUnseenStory;
    }

    public void setHasUnseenStory(boolean hasUnseenStory) {
        this.hasUnseenStory = hasUnseenStory;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}