package com.nexus.dto;

public class ActivitySummaryDto {

    private long totalActivities;
    private long projectsCreated;
    private long collaborationsJoined;
    private long skillsAdded;
    private long commentsPosted;
    private long achievementsAdded;
    private long bountiesParticipated;
    private long connectionsFormed;

    public ActivitySummaryDto() {}

    public ActivitySummaryDto(long totalActivities, long projectsCreated, long collaborationsJoined, long skillsAdded, long commentsPosted, long achievementsAdded, long bountiesParticipated, long connectionsFormed) {
        this.totalActivities = totalActivities;
        this.projectsCreated = projectsCreated;
        this.collaborationsJoined = collaborationsJoined;
        this.skillsAdded = skillsAdded;
        this.commentsPosted = commentsPosted;
        this.achievementsAdded = achievementsAdded;
        this.bountiesParticipated = bountiesParticipated;
        this.connectionsFormed = connectionsFormed;
    }

    public long getTotalActivities() { return totalActivities; }
    public void setTotalActivities(long totalActivities) { this.totalActivities = totalActivities; }

    public long getProjectsCreated() { return projectsCreated; }
    public void setProjectsCreated(long projectsCreated) { this.projectsCreated = projectsCreated; }

    public long getCollaborationsJoined() { return collaborationsJoined; }
    public void setCollaborationsJoined(long collaborationsJoined) { this.collaborationsJoined = collaborationsJoined; }

    public long getSkillsAdded() { return skillsAdded; }
    public void setSkillsAdded(long skillsAdded) { this.skillsAdded = skillsAdded; }

    public long getCommentsPosted() { return commentsPosted; }
    public void setCommentsPosted(long commentsPosted) { this.commentsPosted = commentsPosted; }

    public long getAchievementsAdded() { return achievementsAdded; }
    public void setAchievementsAdded(long achievementsAdded) { this.achievementsAdded = achievementsAdded; }

    public long getBountiesParticipated() { return bountiesParticipated; }
    public void setBountiesParticipated(long bountiesParticipated) { this.bountiesParticipated = bountiesParticipated; }

    public long getConnectionsFormed() { return connectionsFormed; }
    public void setConnectionsFormed(long connectionsFormed) { this.connectionsFormed = connectionsFormed; }
}
