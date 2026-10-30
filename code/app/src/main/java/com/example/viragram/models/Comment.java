package com.example.viragram.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model class representing a comment on a Viragram post (`posts/{postId}/comments/{commentId}`).
 */
public class Comment {

    private String commentId;
    private String postId;
    private String authorId;
    private String authorName;
    private String authorAvatarUrl;
    private int authorAvatarResId;
    private String commentText;

    @ServerTimestamp
    private Date createdAt;

    private String timeAgo;

    public Comment() {
    }

    public Comment(String commentId, String authorName, int authorAvatarResId, String commentText, String timeAgo) {
        this.commentId = commentId;
        this.authorName = authorName;
        this.authorAvatarResId = authorAvatarResId;
        this.commentText = commentText;
        this.timeAgo = timeAgo;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public int getAuthorAvatarResId() {
        return authorAvatarResId;
    }

    public void setAuthorAvatarResId(int authorAvatarResId) {
        this.authorAvatarResId = authorAvatarResId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getTimeAgo() {
        if (timeAgo != null && !timeAgo.isEmpty()) return timeAgo;
        return "Just now";
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }
}