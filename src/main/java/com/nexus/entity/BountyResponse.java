package com.nexus.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bounty_responses")
public class BountyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bounty_id", nullable = false)
    @JsonIgnoreProperties({"responses"})
    private SkillBounty bounty;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responder_id", nullable = false)
    @JsonIgnoreProperties({"password", "userSkills"})
    private User responder;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    // PENDING, ACCEPTED, REJECTED
    @Column(nullable = false)
    private String status = "PENDING";

    private LocalDateTime createdAt = LocalDateTime.now();

    public BountyResponse() {}

    public BountyResponse(SkillBounty bounty, User responder, String message) {
        this.bounty = bounty;
        this.responder = responder;
        this.message = message;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SkillBounty getBounty() { return bounty; }
    public void setBounty(SkillBounty bounty) { this.bounty = bounty; }

    public User getResponder() { return responder; }
    public void setResponder(User responder) { this.responder = responder; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
