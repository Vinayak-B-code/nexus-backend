package com.nexus.dto;

import jakarta.validation.constraints.NotBlank;

public class BountyRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Skill name is required")
    private String skillName;

    // LEARNING, DEBUGGING, CODE_REVIEW, PAIR_PROGRAMMING
    private String bountyType = "DEBUGGING";

    public BountyRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getBountyType() { return bountyType; }
    public void setBountyType(String bountyType) { this.bountyType = bountyType; }
}
