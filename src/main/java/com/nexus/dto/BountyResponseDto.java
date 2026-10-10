package com.nexus.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BountyResponseDto {

    private Long id;
    private String title;
    private String description;
    private String skillName;
    private String bountyType;
    private String status;
    private boolean disabled;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private Long creatorId;
    private String creatorName;
    private String creatorCollege;
    private String creatorAvatarColor;

    private Long acceptedHelperId;
    private String acceptedHelperName;

    private List<HelperOfferDto> responses;
    private int responseCount;

    public static class HelperOfferDto {
        private Long id;
        private Long responderId;
        private String responderName;
        private String responderCollege;
        private String responderAvatarColor;
        private String message;
        private String status;
        private LocalDateTime createdAt;

        public HelperOfferDto() {}

        public HelperOfferDto(Long id, Long responderId, String responderName, String responderCollege, String responderAvatarColor, String message, String status, LocalDateTime createdAt) {
            this.id = id;
            this.responderId = responderId;
            this.responderName = responderName;
            this.responderCollege = responderCollege;
            this.responderAvatarColor = responderAvatarColor;
            this.message = message;
            this.status = status;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getResponderId() { return responderId; }
        public void setResponderId(Long responderId) { this.responderId = responderId; }
        public String getResponderName() { return responderName; }
        public void setResponderName(String responderName) { this.responderName = responderName; }
        public String getResponderCollege() { return responderCollege; }
        public void setResponderCollege(String responderCollege) { this.responderCollege = responderCollege; }
        public String getResponderAvatarColor() { return responderAvatarColor; }
        public void setResponderAvatarColor(String responderAvatarColor) { this.responderAvatarColor = responderAvatarColor; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public BountyResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getBountyType() { return bountyType; }
    public void setBountyType(String bountyType) { this.bountyType = bountyType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public String getCreatorCollege() { return creatorCollege; }
    public void setCreatorCollege(String creatorCollege) { this.creatorCollege = creatorCollege; }
    public String getCreatorAvatarColor() { return creatorAvatarColor; }
    public void setCreatorAvatarColor(String creatorAvatarColor) { this.creatorAvatarColor = creatorAvatarColor; }
    public Long getAcceptedHelperId() { return acceptedHelperId; }
    public void setAcceptedHelperId(Long acceptedHelperId) { this.acceptedHelperId = acceptedHelperId; }
    public String getAcceptedHelperName() { return acceptedHelperName; }
    public void setAcceptedHelperName(String acceptedHelperName) { this.acceptedHelperName = acceptedHelperName; }
    public List<HelperOfferDto> getResponses() { return responses; }
    public void setResponses(List<HelperOfferDto> responses) { this.responses = responses; }
    public int getResponseCount() { return responseCount; }
    public void setResponseCount(int responseCount) { this.responseCount = responseCount; }
}
