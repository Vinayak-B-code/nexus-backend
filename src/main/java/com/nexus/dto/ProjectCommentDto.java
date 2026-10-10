package com.nexus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class ProjectCommentDto {

    private Long id;
    private Long projectId;
    private Long userId;
    private String userName;
    private String userCollege;
    private String userAvatarColor;
    private String content;
    private LocalDateTime createdAt;

    public static class CommentRequest {
        @NotBlank(message = "Comment cannot be blank")
        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        private String content;

        public CommentRequest() {}

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public ProjectCommentDto() {}

    public ProjectCommentDto(Long id, Long projectId, Long userId, String userName, String userCollege, String userAvatarColor, String content, LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.userName = userName;
        this.userCollege = userCollege;
        this.userAvatarColor = userAvatarColor;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserCollege() { return userCollege; }
    public void setUserCollege(String userCollege) { this.userCollege = userCollege; }

    public String getUserAvatarColor() { return userAvatarColor; }
    public void setUserAvatarColor(String userAvatarColor) { this.userAvatarColor = userAvatarColor; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
