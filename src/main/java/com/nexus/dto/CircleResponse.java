package com.nexus.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CircleResponse {

    private Long id;
    private String name;
    private String description;
    private String technologies;
    private int maxMembers;
    private String openPositions;
    private String projectGoal;
    private String progressStatus;
    private boolean disabled;
    private LocalDateTime createdAt;

    private Long creatorId;
    private String creatorName;
    private String creatorCollege;
    private String creatorAvatarColor;

    private List<CircleMemberDto> members;
    private List<MilestoneDto> milestones;
    private int currentMemberCount;
    private int completedMilestonesCount;
    private int totalMilestonesCount;
    private int progressPercentage;

    public static class CircleMemberDto {
        private Long id;
        private Long userId;
        private String userName;
        private String role;
        private LocalDateTime joinedAt;

        public CircleMemberDto() {}

        public CircleMemberDto(Long id, Long userId, String userName, String role, LocalDateTime joinedAt) {
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

    public static class MilestoneDto {
        private Long id;
        private String title;
        private String description;
        private boolean completed;
        private LocalDate dueDate;
        private LocalDateTime completedAt;

        public MilestoneDto() {}

        public MilestoneDto(Long id, String title, String description, boolean completed, LocalDate dueDate, LocalDateTime completedAt) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.completed = completed;
            this.dueDate = dueDate;
            this.completedAt = completedAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    }

    public CircleResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTechnologies() { return technologies; }
    public void setTechnologies(String technologies) { this.technologies = technologies; }
    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int maxMembers) { this.maxMembers = maxMembers; }
    public String getOpenPositions() { return openPositions; }
    public void setOpenPositions(String openPositions) { this.openPositions = openPositions; }
    public String getProjectGoal() { return projectGoal; }
    public void setProjectGoal(String projectGoal) { this.projectGoal = projectGoal; }
    public String getProgressStatus() { return progressStatus; }
    public void setProgressStatus(String progressStatus) { this.progressStatus = progressStatus; }
    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public String getCreatorCollege() { return creatorCollege; }
    public void setCreatorCollege(String creatorCollege) { this.creatorCollege = creatorCollege; }
    public String getCreatorAvatarColor() { return creatorAvatarColor; }
    public void setCreatorAvatarColor(String creatorAvatarColor) { this.creatorAvatarColor = creatorAvatarColor; }
    public List<CircleMemberDto> getMembers() { return members; }
    public void setMembers(List<CircleMemberDto> members) { this.members = members; }
    public List<MilestoneDto> getMilestones() { return milestones; }
    public void setMilestones(List<MilestoneDto> milestones) { this.milestones = milestones; }
    public int getCurrentMemberCount() { return currentMemberCount; }
    public void setCurrentMemberCount(int currentMemberCount) { this.currentMemberCount = currentMemberCount; }
    public int getCompletedMilestonesCount() { return completedMilestonesCount; }
    public void setCompletedMilestonesCount(int completedMilestonesCount) { this.completedMilestonesCount = completedMilestonesCount; }
    public int getTotalMilestonesCount() { return totalMilestonesCount; }
    public void setTotalMilestonesCount(int totalMilestonesCount) { this.totalMilestonesCount = totalMilestonesCount; }
    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }
}
