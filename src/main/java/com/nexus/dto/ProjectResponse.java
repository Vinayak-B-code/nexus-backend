package com.nexus.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectResponse {

    private Long id;
    private String title;
    private String shortDescription;
    private String detailedDescription;
    private String technologies;
    private String category;
    private String githubUrl;
    private String demoUrl;
    private String status;
    private boolean isRescue;
    private String rescueBlockers;
    private String rescueSkillsNeeded;
    private boolean disabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long creatorId;
    private String creatorName;
    private String creatorCollege;
    private String creatorAvatarColor;

    private List<MemberDto> members;
    private List<UpdateDto> updates;
    private int memberCount;
    private long likeCount;
    private long commentCount;
    private boolean likedByCurrentUser;

    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
    public long getCommentCount() { return commentCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }

    public static class MemberDto {
        private Long id;
        private Long userId;
        private String userName;
        private String role;
        private LocalDateTime joinedAt;

        public MemberDto() {}

        public MemberDto(Long id, Long userId, String userName, String role, LocalDateTime joinedAt) {
            this.id = id;
            this.userId = userId;
            this.userName = userName;
            this.role = role;
            this.joinedAt = joinedAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public LocalDateTime getJoinedAt() { return joinedAt; }
        public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    }

    public static class UpdateDto {
        private Long id;
        private String title;
        private String description;
        private String milestones;
        private String challenges;
        private String lessonsLearned;
        private LocalDateTime createdAt;

        public UpdateDto() {}

        public UpdateDto(Long id, String title, String description, String milestones, String challenges, String lessonsLearned, LocalDateTime createdAt) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.milestones = milestones;
            this.challenges = challenges;
            this.lessonsLearned = lessonsLearned;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getMilestones() { return milestones; }
        public void setMilestones(String milestones) { this.milestones = milestones; }
        public String getChallenges() { return challenges; }
        public void setChallenges(String challenges) { this.challenges = challenges; }
        public String getLessonsLearned() { return lessonsLearned; }
        public void setLessonsLearned(String lessonsLearned) { this.lessonsLearned = lessonsLearned; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public ProjectResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public String getDetailedDescription() { return detailedDescription; }
    public void setDetailedDescription(String detailedDescription) { this.detailedDescription = detailedDescription; }
    public String getTechnologies() { return technologies; }
    public void setTechnologies(String technologies) { this.technologies = technologies; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isRescue() { return isRescue; }
    public void setRescue(boolean rescue) { isRescue = rescue; }
    public String getRescueBlockers() { return rescueBlockers; }
    public void setRescueBlockers(String rescueBlockers) { this.rescueBlockers = rescueBlockers; }
    public String getRescueSkillsNeeded() { return rescueSkillsNeeded; }
    public void setRescueSkillsNeeded(String rescueSkillsNeeded) { this.rescueSkillsNeeded = rescueSkillsNeeded; }
    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public String getCreatorCollege() { return creatorCollege; }
    public void setCreatorCollege(String creatorCollege) { this.creatorCollege = creatorCollege; }
    public String getCreatorAvatarColor() { return creatorAvatarColor; }
    public void setCreatorAvatarColor(String creatorAvatarColor) { this.creatorAvatarColor = creatorAvatarColor; }
    public List<MemberDto> getMembers() { return members; }
    public void setMembers(List<MemberDto> members) { this.members = members; }
    public List<UpdateDto> getUpdates() { return updates; }
    public void setUpdates(List<UpdateDto> updates) { this.updates = updates; }
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
}
