package com.nexus.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CircleRequest {

    @NotBlank(message = "Circle name is required")
    private String name;

    private String description;
    private String technologies;

    @Min(value = 2, message = "Maximum members must be at least 2")
    private int maxMembers = 5;

    private String openPositions;
    private String projectGoal;

    private String progressStatus = "PLANNING";

    public CircleRequest() {}

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
}
