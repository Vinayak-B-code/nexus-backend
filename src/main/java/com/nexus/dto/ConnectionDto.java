package com.nexus.dto;

import java.time.LocalDateTime;

public class ConnectionDto {

    private Long id;
    private String status;
    private LocalDateTime createdAt;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserCollege;
    private String otherUserAvatarColor;
    private String otherUserBio;
    private boolean sentByMe;

    public ConnectionDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }
    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }
    public String getOtherUserCollege() { return otherUserCollege; }
    public void setOtherUserCollege(String otherUserCollege) { this.otherUserCollege = otherUserCollege; }
    public String getOtherUserAvatarColor() { return otherUserAvatarColor; }
    public void setOtherUserAvatarColor(String otherUserAvatarColor) { this.otherUserAvatarColor = otherUserAvatarColor; }
    public String getOtherUserBio() { return otherUserBio; }
    public void setOtherUserBio(String otherUserBio) { this.otherUserBio = otherUserBio; }
    public boolean isSentByMe() { return sentByMe; }
    public void setSentByMe(boolean sentByMe) { this.sentByMe = sentByMe; }
}
