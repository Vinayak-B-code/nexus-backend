package com.nexus.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class MilestoneRequest {

    @NotBlank(message = "Milestone title is required")
    private String title;

    private String description;
    private LocalDate dueDate;

    public MilestoneRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
