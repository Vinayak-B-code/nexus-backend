package com.nexus.dto;

import java.util.List;

public class CompatibilityResponse {

    private Long targetUserId;
    private String targetUserName;
    private int score; // 0 to 100
    private String matchExplanation;
    private String projectChemistrySuggestion;
    private List<String> sharedSkills;
    private List<String> complementarySkills;
    private List<String> reciprocalOpportunities;
    private boolean isPreliminary;

    public CompatibilityResponse() {}

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getMatchExplanation() { return matchExplanation; }
    public void setMatchExplanation(String matchExplanation) { this.matchExplanation = matchExplanation; }

    public String getProjectChemistrySuggestion() { return projectChemistrySuggestion; }
    public void setProjectChemistrySuggestion(String projectChemistrySuggestion) { this.projectChemistrySuggestion = projectChemistrySuggestion; }

    public List<String> getSharedSkills() { return sharedSkills; }
    public void setSharedSkills(List<String> sharedSkills) { this.sharedSkills = sharedSkills; }

    public List<String> getComplementarySkills() { return complementarySkills; }
    public void setComplementarySkills(List<String> complementarySkills) { this.complementarySkills = complementarySkills; }

    public List<String> getReciprocalOpportunities() { return reciprocalOpportunities; }
    public void setReciprocalOpportunities(List<String> reciprocalOpportunities) { this.reciprocalOpportunities = reciprocalOpportunities; }

    public boolean isPreliminary() { return isPreliminary; }
    public void setPreliminary(boolean preliminary) { isPreliminary = preliminary; }
}
