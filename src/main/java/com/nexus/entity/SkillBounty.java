package com.nexus.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "skill_bounties")
public class SkillBounty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String skillName;

    // LEARNING, DEBUGGING, CODE_REVIEW, PAIR_PROGRAMMING
    @Column(nullable = false)
    private String bountyType = "DEBUGGING";

    // OPEN, IN_PROGRESS, COMPLETED
    @Column(nullable = false)
    private String status = "OPEN";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({"password", "userSkills"})
    private User creator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accepted_helper_id")
    @JsonIgnoreProperties({"password", "userSkills"})
    private User acceptedHelper;

    private boolean disabled = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "bounty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BountyResponse> responses = new ArrayList<>();

    public SkillBounty() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getBountyType() { return bountyType; }
    public void setBountyType(String bountyType) { this.bountyType = bountyType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public User getAcceptedHelper() { return acceptedHelper; }
    public void setAcceptedHelper(User acceptedHelper) { this.acceptedHelper = acceptedHelper; }

    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<BountyResponse> getResponses() { return responses; }
    public void setResponses(List<BountyResponse> responses) { this.responses = responses; }
}
