package com.nexus.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String shortDescription;
    private String detailedDescription;
    private String technologies;
    private String category;
    private String githubUrl;
    private String demoUrl;

    // IDEA, IN_PROGRESS, COMPLETED, SEEKING_COLLABORATORS, NEEDS_RESCUE
    private String status = "IN_PROGRESS";

    private boolean isRescue = false;
    private String rescueBlockers;
    private String rescueSkillsNeeded;

    public ProjectRequest() {}

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
}
