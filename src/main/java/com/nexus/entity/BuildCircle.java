package com.nexus.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "build_circles")
public class BuildCircle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String technologies;

    private int maxMembers = 5;

    private String openPositions;

    @Column(columnDefinition = "TEXT")
    private String projectGoal;

    // PLANNING, IN_PROGRESS, TESTING, SHIPPED
    @Column(nullable = false)
    private String progressStatus = "PLANNING";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({"password", "userSkills"})
    private User creator;

    private boolean disabled = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "circle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CircleMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "circle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CircleMilestone> milestones = new ArrayList<>();

    public BuildCircle() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<CircleMember> getMembers() { return members; }
    public void setMembers(List<CircleMember> members) { this.members = members; }

    public List<CircleMilestone> getMilestones() { return milestones; }
    public void setMilestones(List<CircleMilestone> milestones) { this.milestones = milestones; }
}
