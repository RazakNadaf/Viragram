package com.example.viragram.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class representing a social post created on Viragram and persisted in Firestore (`posts/{postId}`).
 */
public class Post {

    private String postId;
    private String authorId;
    private String authorName;
    private String authorUsername;
    private String authorCourseAndYear;
    private String authorAvatarUrl;
    private int authorAvatarResId;
    private int postImageResId;
    private String imageUriString;
    private String caption;
    private String location;

    @ServerTimestamp
    private Date createdAt;

    private long timestamp;
    private String timeAgo;
    private int likeCount;
    private int commentCount;
    private boolean isLiked;
    private boolean isFollowing;
    private List<String> likedBy;
    private List<String> savedBy;
    private List<Comment> commentsList;

    public Post() {
        this.likedBy = new ArrayList<>();
        this.savedBy = new ArrayList<>();
        this.commentsList = new ArrayList<>();
    }

    public Post(String postId, String authorName, String authorCourseAndYear, int authorAvatarResId, int postImageResId, String caption, String timeAgo, int likeCount, int commentCount) {
        this.postId = postId;
        this.authorName = authorName;
        this.authorCourseAndYear = authorCourseAndYear;
        this.authorAvatarResId = authorAvatarResId;
        this.postImageResId = postImageResId;
        this.caption = caption;
        this.timeAgo = timeAgo;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isLiked = false;
        this.isFollowing = false;
        this.likedBy = new ArrayList<>();
        this.savedBy = new ArrayList<>();
        this.commentsList = new ArrayList<>();
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

    public String getPublisherId() {
        return authorId;
    }

    public void setPublisherId(String publisherId) {
        this.authorId = publisherId;
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

    public String getAuthorCourseAndYear() {
        return authorCourseAndYear;
    }

    public void setAuthorCourseAndYear(String authorCourseAndYear) {
        this.authorCourseAndYear = authorCourseAndYear;
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

    public int getPostImageResId() {
        return postImageResId;
    }

    public void setPostImageResId(int postImageResId) {
        this.postImageResId = postImageResId;
    }

    public String getImageUriString() {
        return imageUriString;
    }

    public void setImageUriString(String imageUriString) {
        this.imageUriString = imageUriString;
    }

    public String getImageUrl() {
        return imageUriString;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUriString = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimeAgo() {
        if (timeAgo != null && !timeAgo.isEmpty()) return timeAgo;
        return "Just now";
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public List<String> getLikedBy() {
        if (likedBy == null) likedBy = new ArrayList<>();
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public List<String> getSavedBy() {
        if (savedBy == null) savedBy = new ArrayList<>();
        return savedBy;
    }

    public void setSavedBy(List<String> savedBy) {
        this.savedBy = savedBy;
    }

    public List<Comment> getCommentsList() {
        if (commentsList == null) {
            commentsList = new ArrayList<>();
        }
        return commentsList;
    }

    public void setCommentsList(List<Comment> commentsList) {
        this.commentsList = commentsList;
    }
}