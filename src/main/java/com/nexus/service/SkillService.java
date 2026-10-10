package com.nexus.service;

import com.nexus.dto.AddSkillRequest;
import com.nexus.dto.SkillDto;
import com.nexus.entity.Skill;
import com.nexus.entity.User;
import com.nexus.entity.UserSkill;
import com.nexus.exception.BadRequestException;
import com.nexus.exception.ResourceNotFoundException;
import com.nexus.repository.SkillRepository;
import com.nexus.repository.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserService userService;
    private final ActivityLogService activityLogService;

    public SkillService(SkillRepository skillRepository,
                        UserSkillRepository userSkillRepository,
                        UserService userService,
                        ActivityLogService activityLogService) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    public List<SkillDto> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(s -> new SkillDto(s.getId(), s.getName(), s.getCategory()))
                .collect(Collectors.toList());
    }

    public UserSkill addSkillToUser(Long userId, AddSkillRequest request) {
        User user = userService.findById(userId);
        String skillName = request.getSkillName().trim();

        Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                .orElseGet(() -> {
                    Skill newSkill = new Skill(skillName, request.getCategory() != null ? request.getCategory() : "General");
                    return skillRepository.save(newSkill);
                });

        // Check if already mapped with this skillType
        Optional<UserSkill> existing = userSkillRepository.findByUserIdAndSkillIdAndSkillType(userId, skill.getId(), request.getSkillType());
        if (existing.isPresent()) {
            UserSkill us = existing.get();
            us.setProficiencyLevel(request.getProficiencyLevel());
            UserSkill saved = userSkillRepository.save(us);
            activityLogService.logActivity(user, "SKILL_UPDATE", "Updated proficiency for " + skill.getName() + " to " + request.getProficiencyLevel());
            return saved;
        }

        UserSkill userSkill = new UserSkill(user, skill, request.getSkillType(), request.getProficiencyLevel());
        UserSkill saved = userSkillRepository.save(userSkill);
        activityLogService.logActivity(user, "SKILL_ADD", "Added technical skill: " + skill.getName() + " (" + request.getSkillType() + ")");
        return saved;
    }

    public void removeUserSkill(Long userId, Long userSkillId) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));

        if (!userSkill.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized to remove this skill");
        }

        String skillName = userSkill.getSkill().getName();
        User user = userSkill.getUser();
        userSkillRepository.delete(userSkill);
        activityLogService.logActivity(user, "SKILL_REMOVE", "Removed skill from profile: " + skillName);
    }
}
