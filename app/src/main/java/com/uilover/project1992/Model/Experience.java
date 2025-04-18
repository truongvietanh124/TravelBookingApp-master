package com.uilover.project1992.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Experience {

    private String id;
    private String title;
    private String description;
    private String location;
    private String imageUrl;
    private String userId;
    private String userName;
    private long timestamp;
    private Map<String, Boolean> likes;
    private Map<String, Comment> comments; // Thay đổi từ List sang Map

    // Constructor mặc định (Firebase cần có)
    public Experience() {
        this.id = "";
        this.title = "";
        this.description = "";
        this.location = "";
        this.imageUrl = "";
        this.userId = "";
        this.userName = "";
        this.timestamp = 0;
        this.likes = new HashMap<>();
        this.comments = new HashMap<>(); // Thay đổi từ List sang Map
    }

    // Constructor đầy đủ
    public Experience(String id, String title, String description, String location,
                      String imageUrl, String userId, String userName, long timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.userName = userName;
        this.timestamp = timestamp;
        this.likes = new HashMap<>();
        this.comments = new HashMap<>(); // Thay đổi từ List sang Map
    }

    // Getters và Setters (Firebase yêu cầu nếu bạn không dùng public fields)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    public Map<String, Comment> getComments() {
        return comments;
    }

    public void setComments(Map<String, Comment> comments) {
        this.comments = comments;
    }

    // Phương thức tiện ích để chuyển đổi Map comments thành List
    public List<Comment> getCommentList() {
        List<Comment> commentList = new ArrayList<>();
        if (comments != null) {
            commentList.addAll(comments.values());
        }
        return commentList;
    }
}