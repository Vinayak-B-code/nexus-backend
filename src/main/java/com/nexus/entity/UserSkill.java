package com.nexus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "user_skills")
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    // PROFICIENT, WANTS_TO_LEARN, CAN_TEACH
    @Column(nullable = false)
    private String skillType = "PROFICIENT";

    // BEGINNER, INTERMEDIATE, ADVANCED
    private String proficiencyLevel = "INTERMEDIATE";

    public UserSkill() {}

    public UserSkill(User user, Skill skill, String skillType, String proficiencyLevel) {
        this.user = user;
        this.skill = skill;
        this.skillType = skillType;
        this.proficiencyLevel = proficiencyLevel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public String getSkillType() { return skillType; }
    public void setSkillType(String skillType) { this.skillType = skillType; }

    public String getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
}
