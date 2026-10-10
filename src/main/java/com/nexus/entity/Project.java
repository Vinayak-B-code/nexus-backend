package com.nexus.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String detailedDescription;

    private String technologies;
    private String category;
    private String githubUrl;
    private String demoUrl;

    // IDEA, IN_PROGRESS, COMPLETED, SEEKING_COLLABORATORS, NEEDS_RESCUE
    @Column(nullable = false)
    private String status = "IN_PROGRESS";

    private boolean isRescue = false;

    @Column(columnDefinition = "TEXT")
    private String rescueBlockers;

    private String rescueSkillsNeeded;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({"password", "userSkills"})
    private User creator;

    private boolean disabled = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectUpdate> updates = new ArrayList<>();

    public Project() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ProjectMember> getMembers() { return members; }
    public void setMembers(List<ProjectMember> members) { this.members = members; }

    public List<ProjectUpdate> getUpdates() { return updates; }
    public void setUpdates(List<ProjectUpdate> updates) { this.updates = updates; }
}
