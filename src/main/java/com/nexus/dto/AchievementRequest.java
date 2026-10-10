package com.nexus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class AchievementRequest {

    @NotBlank(message = "Achievement title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @NotBlank(message = "Category is required")
    private String category;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Size(max = 150, message = "Organization cannot exceed 150 characters")
    private String organization;

    private LocalDate achievementDate;

    @Size(max = 500, message = "Proof URL cannot exceed 500 characters")
    private String proofUrl;

    private boolean isPublic = true;

    public AchievementRequest() {}

    public AchievementRequest(String title, String category, String description, String organization, LocalDate achievementDate, String proofUrl, boolean isPublic) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.organization = organization;
        this.achievementDate = achievementDate;
        this.proofUrl = proofUrl;
        this.isPublic = isPublic;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public LocalDate getAchievementDate() { return achievementDate; }
    public void setAchievementDate(LocalDate achievementDate) { this.achievementDate = achievementDate; }

    public String getProofUrl() { return proofUrl; }
    public void setProofUrl(String proofUrl) { this.proofUrl = proofUrl; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }
}
