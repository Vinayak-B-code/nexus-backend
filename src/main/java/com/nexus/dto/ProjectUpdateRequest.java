package com.nexus.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectUpdateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String milestones;
    private String challenges;
    private String lessonsLearned;

    public ProjectUpdateRequest() {}

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
}
