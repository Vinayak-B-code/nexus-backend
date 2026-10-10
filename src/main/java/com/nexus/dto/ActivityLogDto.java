package com.nexus.dto;

import java.time.LocalDateTime;

public class ActivityLogDto {

    private Long id;
    private String activityType;
    private String category;
    private String description;
    private LocalDateTime createdAt;
    private String formattedDate;

    public ActivityLogDto() {}

    public ActivityLogDto(Long id, String activityType, String category, String description, LocalDateTime createdAt, String formattedDate) {
        this.id = id;
        this.activityType = activityType;
        this.category = category;
        this.description = description;
        this.createdAt = createdAt;
        this.formattedDate = formattedDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFormattedDate() { return formattedDate; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }
}
