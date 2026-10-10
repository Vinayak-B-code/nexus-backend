package com.nexus.service;

import com.nexus.dto.CompatibilityResponse;
import com.nexus.entity.User;
import com.nexus.entity.UserSkill;
import com.nexus.repository.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CompatibilityService {

    private final UserSkillRepository userSkillRepository;
    private final UserService userService;

    public CompatibilityService(UserSkillRepository userSkillRepository, UserService userService) {
        this.userSkillRepository = userSkillRepository;
        this.userService = userService;
    }

    public CompatibilityResponse calculateCompatibility(Long currentUserId, Long targetUserId) {
        User currentUser = userService.findById(currentUserId);
        User targetUser = userService.findById(targetUserId);

        List<UserSkill> mySkills = userSkillRepository.findByUserId(currentUserId);
        List<UserSkill> theirSkills = userSkillRepository.findByUserId(targetUserId);

        CompatibilityResponse response = new CompatibilityResponse();
        response.setTargetUserId(targetUser.getId());
        response.setTargetUserName(targetUser.getFullName());

        if (mySkills.isEmpty() || theirSkills.isEmpty()) {
            response.setScore(50);
            response.setPreliminary(true);
            response.setMatchExplanation("Preliminary match: Both developers need to add more skills to their Skill DNA to unlock high-fidelity compatibility scoring.");
            response.setProjectChemistrySuggestion("General Collaboration: Connect to explore shared interests and begin pairing on beginner-friendly projects.");
            response.setSharedSkills(Collections.emptyList());
            response.setComplementarySkills(Collections.emptyList());
            response.setReciprocalOpportunities(Collections.emptyList());
            return response;
        }

        // Categorize skills
        Set<String> myProficient = mySkills.stream()
                .filter(s -> "PROFICIENT".equalsIgnoreCase(s.getSkillType()))
                .map(s -> s.getSkill().getName())
                .collect(Collectors.toSet());

        Set<String> theirProficient = theirSkills.stream()
                .filter(s -> "PROFICIENT".equalsIgnoreCase(s.getSkillType()))
                .map(s -> s.getSkill().getName())
                .collect(Collectors.toSet());

        Set<String> myLearn = mySkills.stream()
                .filter(s -> "WANTS_TO_LEARN".equalsIgnoreCase(s.getSkillType()))
                .map(s -> s.getSkill().getName().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> theirTeach = theirSkills.stream()
                .filter(s -> "CAN_TEACH".equalsIgnoreCase(s.getSkillType()))
                .map(s -> s.getSkill().getName().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> theirLearn = theirSkills.stream()
                .filter(s -> "WANTS_TO_LEARN".equalsIgnoreCase(s.getSkillType()))
                .map(s -> s.getSkill().getName().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> myTeach = mySkills.stream()
                .filter(s -> "CAN_TEACH".equalsIgnoreCase(s.getSkillType()))
                .map(s -> s.getSkill().getName().toLowerCase())
                .collect(Collectors.toSet());

        // Shared skills
        List<String> shared = myProficient.stream()
                .filter(s -> theirProficient.stream().anyMatch(ts -> ts.equalsIgnoreCase(s)))
                .collect(Collectors.toList());
        response.setSharedSkills(shared);

        // Categories
        Set<String> myCategories = mySkills.stream()
                .map(s -> s.getSkill().getCategory())
                .collect(Collectors.toSet());
        Set<String> theirCategories = theirSkills.stream()
                .map(s -> s.getSkill().getCategory())
                .collect(Collectors.toSet());

        int score = 45; // baseline

        // Complementary skill analysis
        List<String> complementary = new ArrayList<>();
        boolean frontBackComp = (myCategories.contains("Frontend") && theirCategories.contains("Backend")) ||
                                (myCategories.contains("Backend") && theirCategories.contains("Frontend"));
        if (frontBackComp) {
            score += 25;
            complementary.add("Frontend + Backend full-stack synergy");
        }

        boolean devOpsComp = (myCategories.contains("Backend") && theirCategories.contains("DevOps")) ||
                             (myCategories.contains("DevOps") && theirCategories.contains("Backend"));
        if (devOpsComp) {
            score += 15;
            complementary.add("Backend architecture + Cloud/DevOps infrastructure");
        }

        boolean aiComp = (myCategories.contains("Data and AI") && (theirCategories.contains("Frontend") || theirCategories.contains("Backend"))) ||
                         (theirCategories.contains("Data and AI") && (myCategories.contains("Frontend") || myCategories.contains("Backend")));
        if (aiComp) {
            score += 20;
            complementary.add("AI/Data science modeling + Application development");
        }

        boolean designComp = (myCategories.contains("Design") && theirCategories.contains("Frontend")) ||
                             (theirCategories.contains("Design") && myCategories.contains("Frontend"));
        if (designComp) {
            score += 15;
            complementary.add("UI/UX Design + Frontend implementation");
        }

        // Shared college or degree bonus
        if (currentUser.getCollege() != null && targetUser.getCollege() != null &&
            currentUser.getCollege().equalsIgnoreCase(targetUser.getCollege())) {
            score += 10;
        }

        // Reciprocal learning opportunities
        List<String> reciprocal = new ArrayList<>();
        for (String learn : myLearn) {
            if (theirTeach.contains(learn) || theirProficient.stream().anyMatch(p -> p.equalsIgnoreCase(learn))) {
                reciprocal.add("You want to learn " + learn + " (they can teach or practice it)");
                score += 10;
            }
        }
        for (String tLearn : theirLearn) {
            if (myTeach.contains(tLearn) || myProficient.stream().anyMatch(p -> p.equalsIgnoreCase(tLearn))) {
                reciprocal.add(targetUser.getFullName() + " wants to learn " + tLearn + " (you can mentor)");
                score += 10;
            }
        }
        response.setReciprocalOpportunities(reciprocal);
        response.setComplementarySkills(complementary);

        score = Math.min(score, 98);
        response.setScore(score);

        // Match Explanation
        StringBuilder explanation = new StringBuilder();
        if (frontBackComp) {
            explanation.append("Exceptional project fit: Complementary full-stack pairing. You and ").append(targetUser.getFullName())
                    .append(" span both client-side and server-side engineering.");
        } else if (!shared.isEmpty()) {
            explanation.append("High stack alignment: Both of you share expertise in ")
                    .append(String.join(", ", shared)).append(", enabling seamless code reviews and paired sprints.");
        } else if (!reciprocal.isEmpty()) {
            explanation.append("Strong reciprocal knowledge exchange: You both offer skills the other seeks to learn.");
        } else {
            explanation.append("Compatible developer profiles with growing overlap across software fundamentals.");
        }
        response.setMatchExplanation(explanation.toString());

        // Project Chemistry Suggestion
        String chemistry;
        if (frontBackComp) {
            chemistry = "Modern Full-Stack SaaS: Collaborate on an end-to-end web product, pairing an intuitive client interface with robust Spring Boot / REST API services.";
        } else if (aiComp) {
            chemistry = "AI-Driven Developer Tool: Combine predictive/generative models with a polished user dashboard to solve real campus problems.";
        } else if (devOpsComp) {
            chemistry = "Scalable Cloud Architecture: Engineer resilient microservices with automated Docker containerization and CI/CD pipelines.";
        } else if (designComp) {
            chemistry = "Design System & Frontend Library: Build a high-polish component showcase and interactive student web portal.";
        } else if (!shared.isEmpty()) {
            chemistry = "Deep Tech Exploration: Co-author an open-source library or utility harnessing your shared mastery of " + shared.get(0) + ".";
        } else {
            chemistry = "Cross-Disciplinary Hackathon Build: Combine diverse perspectives to validate an ambitious MVP in a fast-paced sprint.";
        }
        response.setProjectChemistrySuggestion(chemistry);
        response.setPreliminary(mySkills.size() < 2 || theirSkills.size() < 2);

        return response;
    }
}
