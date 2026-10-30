package com.example.viragram.firebase;

import android.util.Log;

import com.example.viragram.models.Announcement;
import com.example.viragram.models.Club;
import com.example.viragram.models.Comment;
import com.example.viragram.models.Event;
import com.example.viragram.models.Post;
import com.example.viragram.models.Story;
import com.example.viragram.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton repository manager for Viragram Cloud Firestore database operations with detailed diagnostics.
 */
public class FirestoreManager {

    private static final String TAG = "ViragramPostFlow";

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_POSTS = "posts";
    private static final String COLLECTION_STORIES = "stories";
    private static final String COLLECTION_COMMENTS = "comments";
    private static final String COLLECTION_EVENTS = "events";
    private static final String COLLECTION_CLUBS = "clubs";
    private static final String COLLECTION_ANNOUNCEMENTS = "announcements";
    private static final String SUBCOLLECTION_FOLLOWING = "following";
    private static final String SUBCOLLECTION_FOLLOWERS = "followers";

    private static FirestoreManager instance;
    private final FirebaseFirestore firestore;

    private FirestoreManager() {
        firestore = FirebaseFirestore.getInstance();
        try {
            String projectId = FirebaseApp.getInstance().getOptions().getProjectId();
            Log.d(TAG, "[ViragramFirebase] ProjectId=" + projectId + " | Database=(default)");
        } catch (Exception ignored) {
        }
    }

    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public String getCurrentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // ==============================================================================
    // 1. USER PROFILES
    // ==============================================================================

    public void createUserProfile(User user, OnCompleteListener<Void> listener) {
        if (user == null || user.getUid() == null) return;

        Log.d("ViragramProfileSave", "Creating user profile document for UID: " + user.getUid());
        Log.d("ViragramProfileSave", "Path: users/" + user.getUid());

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("fullName", user.getFullName() != null ? user.getFullName() : "Campus Student");
        userData.put("username", user.getUsername() != null ? user.getUsername() : "student.unigoa");
        userData.put("email", user.getEmail() != null ? user.getEmail() : "");
        userData.put("studentId", user.getStudentId() != null ? user.getStudentId() : "");
        userData.put("course", user.getCourse() != null ? user.getCourse() : "Goa University Student");
        userData.put("year", user.getYear() != null ? user.getYear() : "1st Year");
        userData.put("university", user.getUniversity() != null ? user.getUniversity() : "Goa University");
        userData.put("bio", user.getBio() != null ? user.getBio() : "");
        userData.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        userData.put("role", user.getRole() != null ? user.getRole() : "student");
        userData.put("followerCount", user.getFollowerCount());
        userData.put("followingCount", user.getFollowingCount());
        userData.put("postCount", user.getPostCount());
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .set(userData, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    Log.d("ViragramProfileSave", "Success=" + task.isSuccessful());
                    if (!task.isSuccessful() && task.getException() != null) {
                        Log.e("ViragramProfileSave", "Exception=" + task.getException().getMessage());
                    }
                    if (listener != null) {
                        listener.onComplete(task);
                    }
                });
    }

    public ListenerRegistration getUserProfileListener(String uid, EventListener<DocumentSnapshot> listener) {
        if (uid == null || uid.isEmpty()) return null;
        return firestore.collection(COLLECTION_USERS)
                .document(uid)
                .addSnapshotListener(listener);
    }

    public void getUserProfile(String uid, OnCompleteListener<DocumentSnapshot> listener) {
        if (uid == null || uid.isEmpty()) return;
        firestore.collection(COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnCompleteListener(listener);
    }

    public void updateUserProfile(String uid, Map<String, Object> updates, OnCompleteListener<Void> listener) {
        if (uid == null || uid.isEmpty() || updates == null) return;

        updates.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection(COLLECTION_USERS)
                .document(uid)
                .set(updates, SetOptions.merge())
                .addOnCompleteListener(listener);
    }

    // ==============================================================================
    // 2. POSTS
    // ==============================================================================

    public void createPost(Post post, OnCompleteListener<Void> listener) {
        if (post == null || post.getPostId() == null) {
            Log.e(TAG, "[ViragramPostSave] Post object is null or missing ID");
            return;
        }

        String authorId = post.getAuthorId() != null ? post.getAuthorId() : getCurrentUid();

        Map<String, Object> postData = new HashMap<>();
        postData.put("postId", post.getPostId());
        postData.put("authorId", authorId);
        postData.put("authorName", post.getAuthorName() != null ? post.getAuthorName() : "Campus Student");
        postData.put("authorUsername", post.getAuthorUsername() != null ? post.getAuthorUsername() : "student.unigoa");
        postData.put("authorCourseAndYear", post.getAuthorCourseAndYear() != null ? post.getAuthorCourseAndYear() : "Goa University Student");
        postData.put("authorAvatarUrl", post.getAuthorAvatarUrl() != null ? post.getAuthorAvatarUrl() : "");
        postData.put("imageUriString", post.getImageUriString() != null ? post.getImageUriString() : "");
        postData.put("caption", post.getCaption() != null ? post.getCaption() : "");
        postData.put("location", post.getLocation() != null ? post.getLocation() : "Goa University Campus");
        postData.put("createdAt", FieldValue.serverTimestamp());
        postData.put("likeCount", post.getLikeCount());
        postData.put("commentCount", post.getCommentCount());
        postData.put("likedBy", new ArrayList<String>());
        postData.put("savedBy", new ArrayList<String>());

        Log.d(TAG, "[ViragramPostSave] Writing path=" + COLLECTION_POSTS + "/" + post.getPostId());

        firestore.collection(COLLECTION_POSTS)
                .document(post.getPostId())
                .set(postData, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "[ViragramPostSave] SUCCESS " + COLLECTION_POSTS + "/" + post.getPostId());
                        if (authorId != null) {
                            // Separately update post count without tying it to the main success callback
                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put("postCount", FieldValue.increment(1));
                            firestore.collection(COLLECTION_USERS)
                                    .document(authorId)
                                    .set(userUpdates, SetOptions.merge());
                        }
                    } else {
                        Log.e(TAG, "[ViragramPostSave] FAILURE");
                        if (task.getException() != null) {
                            Log.e(TAG, "[ViragramPostSave] ExceptionClass=" + task.getException().getClass().getSimpleName());
                            Log.e(TAG, "[ViragramPostSave] ExceptionMessage=" + task.getException().getMessage());
                        }
                    }
                    if (listener != null) {
                        listener.onComplete(task);
                    }
                });
    }

    public ListenerRegistration getFeedPostsListener(EventListener<QuerySnapshot> listener) {
        return firestore.collection(COLLECTION_POSTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    public ListenerRegistration getUserPostsListener(String uid, EventListener<QuerySnapshot> listener) {
        if (uid == null || uid.isEmpty()) return null;
        return firestore.collection(COLLECTION_POSTS)
                .whereEqualTo("authorId", uid)
                .addSnapshotListener(listener);
    }

    public void toggleLikePost(String postId, String userId, boolean isLiked, OnCompleteListener<Void> listener) {
        if (postId == null || userId == null) return;

        Map<String, Object> updates = new HashMap<>();
        if (isLiked) {
            updates.put("likedBy", FieldValue.arrayUnion(userId));
            updates.put("likeCount", FieldValue.increment(1));
        } else {
            updates.put("likedBy", FieldValue.arrayRemove(userId));
            updates.put("likeCount", FieldValue.increment(-1));
        }

        firestore.collection(COLLECTION_POSTS)
                .document(postId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    // ==============================================================================
    // 3. FOLLOW RELATIONSHIPS
    // ==============================================================================

    public void followUser(String myUid, String myName, String targetUid, String targetName, OnCompleteListener<Void> listener) {
        if (myUid == null || targetUid == null || myUid.equals(targetUid)) return;

        checkIsFollowing(myUid, targetUid, checkTask -> {
            if (checkTask.isSuccessful() && checkTask.getResult() != null && checkTask.getResult().exists()) {
                return;
            }

            WriteBatch batch = firestore.batch();

            Map<String, Object> followingData = new HashMap<>();
            followingData.put("targetUid", targetUid);
            followingData.put("targetName", targetName != null ? targetName : "Student");
            followingData.put("createdAt", FieldValue.serverTimestamp());
            batch.set(firestore.collection(COLLECTION_USERS).document(myUid).collection(SUBCOLLECTION_FOLLOWING).document(targetUid), followingData);

            Map<String, Object> followerData = new HashMap<>();
            followerData.put("followerUid", myUid);
            followerData.put("followerName", myName != null ? myName : "Student");
            followerData.put("createdAt", FieldValue.serverTimestamp());
            batch.set(firestore.collection(COLLECTION_USERS).document(targetUid).collection(SUBCOLLECTION_FOLLOWERS).document(myUid), followerData);

            Map<String, Object> myUpdates = new HashMap<>();
            myUpdates.put("followingCount", FieldValue.increment(1));
            batch.set(firestore.collection(COLLECTION_USERS).document(myUid), myUpdates, SetOptions.merge());
            
            Map<String, Object> targetUpdates = new HashMap<>();
            targetUpdates.put("followerCount", FieldValue.increment(1));
            batch.set(firestore.collection(COLLECTION_USERS).document(targetUid), targetUpdates, SetOptions.merge());

            batch.commit().addOnCompleteListener(listener);
        });
    }

    public void unfollowUser(String myUid, String targetUid, OnCompleteListener<Void> listener) {
        if (myUid == null || targetUid == null || myUid.equals(targetUid)) return;

        WriteBatch batch = firestore.batch();

        batch.delete(firestore.collection(COLLECTION_USERS).document(myUid).collection(SUBCOLLECTION_FOLLOWING).document(targetUid));
        batch.delete(firestore.collection(COLLECTION_USERS).document(targetUid).collection(SUBCOLLECTION_FOLLOWERS).document(myUid));

        Map<String, Object> myUpdates = new HashMap<>();
        myUpdates.put("followingCount", FieldValue.increment(-1));
        batch.set(firestore.collection(COLLECTION_USERS).document(myUid), myUpdates, SetOptions.merge());
        
        Map<String, Object> targetUpdates = new HashMap<>();
        targetUpdates.put("followerCount", FieldValue.increment(-1));
        batch.set(firestore.collection(COLLECTION_USERS).document(targetUid), targetUpdates, SetOptions.merge());

        batch.commit().addOnCompleteListener(listener);
    }

    public void checkIsFollowing(String myUid, String targetUid, OnCompleteListener<DocumentSnapshot> listener) {
        if (myUid == null || targetUid == null) return;
        firestore.collection(COLLECTION_USERS)
                .document(myUid)
                .collection(SUBCOLLECTION_FOLLOWING)
                .document(targetUid)
                .get()
                .addOnCompleteListener(listener);
    }

    // ==============================================================================
    // 4. STORIES
    // ==============================================================================

    public void createStory(Story story, OnCompleteListener<Void> listener) {
        if (story == null || story.getStoryId() == null) return;

        Map<String, Object> storyData = new HashMap<>();
        storyData.put("storyId", story.getStoryId());
        storyData.put("authorId", story.getAuthorId() != null ? story.getAuthorId() : getCurrentUid());
        storyData.put("authorName", story.getAuthorName() != null ? story.getAuthorName() : "Campus Student");
        storyData.put("authorUsername", story.getAuthorUsername() != null ? story.getAuthorUsername() : "student.unigoa");
        storyData.put("authorAvatarUrl", story.getAuthorAvatarUrl() != null ? story.getAuthorAvatarUrl() : "");
        storyData.put("imageUrl", story.getImageUrl() != null ? story.getImageUrl() : "");
        storyData.put("createdAt", FieldValue.serverTimestamp());
        storyData.put("expiresAt", new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000L));

        firestore.collection(COLLECTION_STORIES)
                .document(story.getStoryId())
                .set(storyData, SetOptions.merge())
                .addOnCompleteListener(listener);
    }

    public ListenerRegistration getActiveStoriesListener(EventListener<QuerySnapshot> listener) {
        return firestore.collection(COLLECTION_STORIES)
                .whereGreaterThan("expiresAt", new Date())
                .addSnapshotListener(listener);
    }

    // ==============================================================================
    // 5. COMMENTS
    // ==============================================================================

    public void addComment(String postId, Comment comment, OnCompleteListener<Void> listener) {
        if (postId == null || comment == null) return;

        String commentId = "c_" + System.currentTimeMillis();
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("commentId", commentId);
        commentData.put("postId", postId);
        commentData.put("authorId", comment.getAuthorId() != null ? comment.getAuthorId() : getCurrentUid());
        commentData.put("authorName", comment.getAuthorName() != null ? comment.getAuthorName() : "Campus Student");
        commentData.put("authorAvatarUrl", comment.getAuthorAvatarUrl() != null ? comment.getAuthorAvatarUrl() : "");
        commentData.put("commentText", comment.getCommentText() != null ? comment.getCommentText() : "");
        commentData.put("createdAt", FieldValue.serverTimestamp());

        firestore.collection(COLLECTION_POSTS)
                .document(postId)
                .collection(COLLECTION_COMMENTS)
                .document(commentId)
                .set(commentData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> postUpdates = new HashMap<>();
                        postUpdates.put("commentCount", FieldValue.increment(1));
                        firestore.collection(COLLECTION_POSTS)
                                .document(postId)
                                .set(postUpdates, SetOptions.merge());
                    }
                    if (listener != null) {
                        listener.onComplete(task);
                    }
                });
    }

    public ListenerRegistration getCommentsListener(String postId, EventListener<QuerySnapshot> listener) {
        return firestore.collection(COLLECTION_POSTS)
                .document(postId)
                .collection(COLLECTION_COMMENTS)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }

    // ==============================================================================
    // 6. COLLEGE HUB: EVENTS, ANNOUNCEMENTS, CLUBS
    // ==============================================================================

    public ListenerRegistration getPublishedEventsListener(EventListener<QuerySnapshot> listener) {
        return firestore.collection(COLLECTION_EVENTS)
                .whereEqualTo("published", true)
                .addSnapshotListener(listener);
    }

    public ListenerRegistration getPublishedAnnouncementsListener(EventListener<QuerySnapshot> listener) {
        return firestore.collection(COLLECTION_ANNOUNCEMENTS)
                .whereEqualTo("published", true)
                .addSnapshotListener(listener);
    }

    public ListenerRegistration getClubsListener(EventListener<QuerySnapshot> listener) {
        return firestore.collection(COLLECTION_CLUBS)
                .addSnapshotListener(listener);
    }

    public void createEvent(Event event, OnCompleteListener<Void> listener) {
        if (event == null || event.getEventId() == null) return;
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventId", event.getEventId());
        eventData.put("title", event.getTitle() != null ? event.getTitle() : "");
        eventData.put("category", event.getCategory() != null ? event.getCategory() : "");
        eventData.put("date", event.getDate() != null ? event.getDate() : "");
        eventData.put("time", event.getTime() != null ? event.getTime() : "");
        eventData.put("location", event.getLocation() != null ? event.getLocation() : "");
        eventData.put("organizer", event.getOrganizer() != null ? event.getOrganizer() : "");
        eventData.put("description", event.getDescription() != null ? event.getDescription() : "");
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            eventData.put("imageUrl", event.getImageUrl());
        }
        eventData.put("published", event.isPublished());
        eventData.put("createdBy", event.getCreatedBy());
        
        // This is a create, so we set createdAt
        eventData.put("createdAt", FieldValue.serverTimestamp());
        eventData.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection(COLLECTION_EVENTS)
                .document(event.getEventId())
                .set(eventData, SetOptions.merge())
                .addOnCompleteListener(listener);
    }
    
    public void updateEvent(Event event, OnCompleteListener<Void> listener) {
        if (event == null || event.getEventId() == null) return;
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", event.getTitle() != null ? event.getTitle() : "");
        eventData.put("category", event.getCategory() != null ? event.getCategory() : "");
        eventData.put("date", event.getDate() != null ? event.getDate() : "");
        eventData.put("time", event.getTime() != null ? event.getTime() : "");
        eventData.put("location", event.getLocation() != null ? event.getLocation() : "");
        eventData.put("organizer", event.getOrganizer() != null ? event.getOrganizer() : "");
        eventData.put("description", event.getDescription() != null ? event.getDescription() : "");
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            eventData.put("imageUrl", event.getImageUrl());
        }
        eventData.put("published", event.isPublished());
        
        // This is an update, only set updatedAt
        eventData.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection(COLLECTION_EVENTS)
                .document(event.getEventId())
                .set(eventData, SetOptions.merge())
                .addOnCompleteListener(listener);
    }
    
    public void deleteEvent(String eventId, OnCompleteListener<Void> listener) {
        if (eventId == null || eventId.isEmpty()) return;
        firestore.collection(COLLECTION_EVENTS)
                .document(eventId)
                .delete()
                .addOnCompleteListener(listener);
    }
    
    public void createAnnouncement(Announcement announcement, OnCompleteListener<Void> listener) {
        if (announcement == null || announcement.getId() == null) return;
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", announcement.getId());
        data.put("title", announcement.getTitle() != null ? announcement.getTitle() : "");
        data.put("category", announcement.getCategory() != null ? announcement.getCategory() : "GENERAL");
        data.put("description", announcement.getDescription() != null ? announcement.getDescription() : "");
        data.put("priority", announcement.getPriority() != null ? announcement.getPriority() : "NORMAL");
        data.put("author", announcement.getAuthor() != null ? announcement.getAuthor() : "Campus Admin");
        data.put("timestamp", announcement.getTimestamp() != null ? announcement.getTimestamp() : "Just now");
        
        if (announcement.getImageUrl() != null && !announcement.getImageUrl().isEmpty()) {
            data.put("imageUrl", announcement.getImageUrl());
        }
        
        data.put("published", announcement.isPublished());
        data.put("createdBy", announcement.getCreatedBy());
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection(COLLECTION_ANNOUNCEMENTS)
                .document(announcement.getId())
                .set(data, SetOptions.merge())
                .addOnCompleteListener(listener);
    }
    
    public void updateAnnouncement(Announcement announcement, OnCompleteListener<Void> listener) {
        if (announcement == null || announcement.getId() == null) return;
        
        Map<String, Object> data = new HashMap<>();
        data.put("title", announcement.getTitle() != null ? announcement.getTitle() : "");
        data.put("category", announcement.getCategory() != null ? announcement.getCategory() : "GENERAL");
        data.put("description", announcement.getDescription() != null ? announcement.getDescription() : "");
        data.put("priority", announcement.getPriority() != null ? announcement.getPriority() : "NORMAL");
        data.put("author", announcement.getAuthor() != null ? announcement.getAuthor() : "Campus Admin");
        data.put("timestamp", announcement.getTimestamp() != null ? announcement.getTimestamp() : "Just now");
        
        if (announcement.getImageUrl() != null && !announcement.getImageUrl().isEmpty()) {
            data.put("imageUrl", announcement.getImageUrl());
        }
        
        data.put("published", announcement.isPublished());
        data.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection(COLLECTION_ANNOUNCEMENTS)
                .document(announcement.getId())
                .set(data, SetOptions.merge())
                .addOnCompleteListener(listener);
    }
    
    public void deleteAnnouncement(String announcementId, OnCompleteListener<Void> listener) {
        if (announcementId == null || announcementId.isEmpty()) return;
        firestore.collection(COLLECTION_ANNOUNCEMENTS)
                .document(announcementId)
                .delete()
                .addOnCompleteListener(listener);
    }

    public void createClub(Club club, OnCompleteListener<Void> listener) {
        if (club == null || club.getClubId() == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("clubId", club.getClubId());
        data.put("name", club.getName() != null ? club.getName() : "");
        data.put("category", club.getCategory() != null ? club.getCategory() : "");
        data.put("coordinator", club.getCoordinator() != null ? club.getCoordinator() : "");
        data.put("description", club.getDescription() != null ? club.getDescription() : "");
        data.put("memberCount", club.getMemberCount());
        data.put("status", club.getStatus() != null ? club.getStatus() : "ACTIVE");

        if (club.getImageUrl() != null && !club.getImageUrl().isEmpty()) {
            data.put("imageUrl", club.getImageUrl());
        }
        
        data.put("createdBy", club.getCreatedBy());
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection(COLLECTION_CLUBS)
                .document(club.getClubId())
                .set(data, SetOptions.merge())
                .addOnCompleteListener(listener);
    }
    
    public void updateClub(Club club, OnCompleteListener<Void> listener) {
        if (club == null || club.getClubId() == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", club.getName() != null ? club.getName() : "");
        data.put("category", club.getCategory() != null ? club.getCategory() : "");
        data.put("coordinator", club.getCoordinator() != null ? club.getCoordinator() : "");
        data.put("description", club.getDescription() != null ? club.getDescription() : "");
        data.put("memberCount", club.getMemberCount());
        data.put("status", club.getStatus() != null ? club.getStatus() : "ACTIVE");

        if (club.getImageUrl() != null && !club.getImageUrl().isEmpty()) {
            data.put("imageUrl", club.getImageUrl());
        }
        
        data.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection(COLLECTION_CLUBS)
                .document(club.getClubId())
                .set(data, SetOptions.merge())
                .addOnCompleteListener(listener);
    }
    
    public void deleteClub(String clubId, OnCompleteListener<Void> listener) {
        if (clubId == null || clubId.isEmpty()) return;
        firestore.collection(COLLECTION_CLUBS)
                .document(clubId)
                .delete()
                .addOnCompleteListener(listener);
    }
}