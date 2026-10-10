package com.nexus.dto;

import jakarta.validation.constraints.NotBlank;

public class AddSkillRequest {

    @NotBlank(message = "Skill name is required")
    private String skillName;

    private String category = "GENERAL";

    // PROFICIENT, WANTS_TO_LEARN, CAN_TEACH
    private String skillType = "PROFICIENT";

    // BEGINNER, INTERMEDIATE, ADVANCED
    private String proficiencyLevel = "INTERMEDIATE";

    public AddSkillRequest() {}

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSkillType() { return skillType; }
    public void setSkillType(String skillType) { this.skillType = skillType; }

    public String getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
}
