package com.example.viragram.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model class representing a Viragram user profile document in Firestore (`users/{uid}`).
 */
public class User {

    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_CLUB_ADMIN = "club_admin";
    public static final String ROLE_COLLEGE_ADMIN = "college_admin";

    private String uid;
    private String fullName;
    private String username;
    private String email;
    private String studentId;
    private String course;
    private String year;
    private String university;
    private String bio;
    private String avatarUrl;
    private String role;
    private int followerCount;
    private int followingCount;
    private int postCount;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    public User() {
        this.role = ROLE_STUDENT;
    }

    public User(String uid, String username, String fullName, String email, String avatarUrl, String course, String university) {
        this.uid = uid;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.course = course;
        this.university = university;
        this.bio = "";
        this.studentId = "";
        this.year = "";
        this.role = ROLE_STUDENT;
        this.followerCount = 0;
        this.followingCount = 0;
        this.postCount = 0;
    }

    public User(String uid, String fullName, String username, String email, String studentId, String course, String year, String university, String bio, String avatarUrl) {
        this.uid = uid;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.studentId = studentId;
        this.course = course;
        this.year = year;
        this.university = university;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.role = ROLE_STUDENT;
        this.followerCount = 0;
        this.followingCount = 0;
        this.postCount = 0;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserId() {
        return uid;
    }

    public void setUserId(String userId) {
        this.uid = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getCollegeName() {
        return university;
    }

    public void setCollegeName(String collegeName) {
        this.university = collegeName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getProfileImageUrl() {
        return avatarUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.avatarUrl = profileImageUrl;
    }

    public String getRole() {
        return role != null ? role : ROLE_STUDENT;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isCollegeAdmin() {
        return ROLE_COLLEGE_ADMIN.equalsIgnoreCase(this.role);
    }

    public boolean isClubAdmin() {
        return ROLE_CLUB_ADMIN.equalsIgnoreCase(this.role) || isCollegeAdmin();
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}