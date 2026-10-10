package com.nexus.dto;

import java.time.LocalDateTime;

public class CircleJoinRequestDto {

    private Long id;
    private Long circleId;
    private String circleName;
    private Long requesterId;
    private String requesterName;
    private String requesterCollege;
    private String requesterAvatarColor;
    private String message;
    private String status;
    private LocalDateTime createdAt;

    public CircleJoinRequestDto() {}

    public CircleJoinRequestDto(Long id, Long circleId, String circleName, Long requesterId, String requesterName, String requesterCollege, String requesterAvatarColor, String message, String status, LocalDateTime createdAt) {
        this.id = id;
        this.circleId = circleId;
        this.circleName = circleName;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.requesterCollege = requesterCollege;
        this.requesterAvatarColor = requesterAvatarColor;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCircleId() { return circleId; }
    public void setCircleId(Long circleId) { this.circleId = circleId; }
    public String getCircleName() { return circleName; }
    public void setCircleName(String circleName) { this.circleName = circleName; }
    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getRequesterCollege() { return requesterCollege; }
    public void setRequesterCollege(String requesterCollege) { this.requesterCollege = requesterCollege; }
    public String getRequesterAvatarColor() { return requesterAvatarColor; }
    public void setRequesterAvatarColor(String requesterAvatarColor) { this.requesterAvatarColor = requesterAvatarColor; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
