package com.nexus.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String college;
    private String degree;
    private String studyYear;
    private String bio;
    private String githubUrl;
    private String linkedinUrl;
    private String avatarColor;
    private String role;
    private LocalDateTime createdAt;

    private List<UserSkillDto> skills;
    private Map<String, Integer> skillDna; // Category -> count
    private int projectCount;
    private int circleCount;
    private int connectionCount;

    public static class UserSkillDto {
        private Long id;
        private Long skillId;
        private String skillName;
        private String category;
        private String skillType; // PROFICIENT, WANTS_TO_LEARN, CAN_TEACH
        private String proficiencyLevel; // BEGINNER, INTERMEDIATE, ADVANCED

        public UserSkillDto() {}

        public UserSkillDto(Long id, Long skillId, String skillName, String category, String skillType, String proficiencyLevel) {
            this.id = id;
            this.skillId = skillId;
            this.skillName = skillName;
            this.category = category;
            this.skillType = skillType;
            this.proficiencyLevel = proficiencyLevel;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getSkillId() { return skillId; }
        public void setSkillId(Long skillId) { this.skillId = skillId; }
        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSkillType() { return skillType; }
        public void setSkillType(String skillType) { this.skillType = skillType; }
        public String getProficiencyLevel() { return proficiencyLevel; }
        public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
    }

    public UserResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public String getStudyYear() { return studyYear; }
    public void setStudyYear(String studyYear) { this.studyYear = studyYear; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<UserSkillDto> getSkills() { return skills; }
    public void setSkills(List<UserSkillDto> skills) { this.skills = skills; }
    public Map<String, Integer> getSkillDna() { return skillDna; }
    public void setSkillDna(Map<String, Integer> skillDna) { this.skillDna = skillDna; }
    public int getProjectCount() { return projectCount; }
    public void setProjectCount(int projectCount) { this.projectCount = projectCount; }
    public int getCircleCount() { return circleCount; }
    public void setCircleCount(int circleCount) { this.circleCount = circleCount; }
    public int getConnectionCount() { return connectionCount; }
    public void setConnectionCount(int connectionCount) { this.connectionCount = connectionCount; }
}
