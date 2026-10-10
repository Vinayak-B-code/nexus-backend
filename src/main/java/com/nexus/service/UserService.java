package com.nexus.service;

import com.nexus.dto.ProfileUpdateRequest;
import com.nexus.dto.RegisterRequest;
import com.nexus.dto.UserResponse;
import com.nexus.entity.User;
import com.nexus.entity.UserSkill;
import com.nexus.exception.BadRequestException;
import com.nexus.exception.ResourceNotFoundException;
import com.nexus.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final ProjectRepository projectRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final ConnectionRepository connectionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    private static final String[] AVATAR_COLORS = {
        "#625BEE", "#4F46E5", "#0D9488", "#E11D48", "#D97706", "#2563EB", "#7C3AED", "#059669"
    };

    private final SkillRepository skillRepository;

    public UserService(UserRepository userRepository,
                       UserSkillRepository userSkillRepository,
                       ProjectRepository projectRepository,
                       CircleMemberRepository circleMemberRepository,
                       ConnectionRepository connectionRepository,
                       PasswordEncoder passwordEncoder,
                       ActivityLogService activityLogService,
                       SkillRepository skillRepository) {
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.projectRepository = projectRepository;
        this.circleMemberRepository = circleMemberRepository;
        this.connectionRepository = connectionRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
        this.skillRepository = skillRepository;
    }

    public User registerUser(RegisterRequest request) {
        if (request.getConfirmPassword() != null && !request.getConfirmPassword().isBlank()) {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new BadRequestException("Passwords do not match");
            }
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCollege(request.getCollege() != null ? request.getCollege().trim() : null);
        user.setDegree(request.getDegree() != null ? request.getDegree().trim() : null);
        user.setStudyYear(request.getStudyYear() != null ? request.getStudyYear() : "Year 1");
        user.setBio(request.getBio() != null ? request.getBio().trim() : null);

        // Assign a consistent distinct avatar color
        int colorIdx = Math.abs(user.getEmail().hashCode()) % AVATAR_COLORS.length;
        user.setAvatarColor(AVATAR_COLORS[colorIdx]);

        // First user becomes admin, others user
        if (userRepository.count() == 0) {
            user.setRole("ROLE_ADMIN");
        } else {
            user.setRole("ROLE_USER");
        }

        User savedUser = userRepository.save(user);

        // If user submitted skills during registration, save them
        if (request.getSkills() != null && !request.getSkills().isBlank()) {
            String[] skillTokens = request.getSkills().split("[,;]");
            for (String raw : skillTokens) {
                String token = raw.trim();
                if (!token.isBlank()) {
                    com.nexus.entity.Skill skill = skillRepository.findByNameIgnoreCase(token)
                            .orElseGet(() -> skillRepository.save(new com.nexus.entity.Skill(token, "General")));
                    UserSkill us = new UserSkill(savedUser, skill, "PROFICIENT", "INTERMEDIATE");
                    userSkillRepository.save(us);
                }
            }
        }

        return savedUser;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = findById(userId);
        user.setFullName(request.getFullName().trim());
        user.setCollege(request.getCollege());
        user.setDegree(request.getDegree());
        user.setStudyYear(request.getStudyYear());
        user.setBio(request.getBio());
        user.setGithubUrl(request.getGithubUrl());
        user.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getAvatarColor() != null && !request.getAvatarColor().isBlank()) {
            user.setAvatarColor(request.getAvatarColor());
        }
        User saved = userRepository.save(user);
        activityLogService.logActivity(saved, "PROFILE_UPDATE", "Updated builder profile details");
        return saved;
    }

    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setCollege(user.getCollege());
        response.setDegree(user.getDegree());
        response.setStudyYear(user.getStudyYear());
        response.setBio(user.getBio());
        response.setGithubUrl(user.getGithubUrl());
        response.setLinkedinUrl(user.getLinkedinUrl());
        response.setAvatarColor(user.getAvatarColor() != null ? user.getAvatarColor() : AVATAR_COLORS[0]);
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());

        List<UserSkill> skills = userSkillRepository.findByUserId(user.getId());
        List<UserResponse.UserSkillDto> skillDtos = skills.stream().map(us ->
                new UserResponse.UserSkillDto(
                        us.getId(),
                        us.getSkill().getId(),
                        us.getSkill().getName(),
                        us.getSkill().getCategory(),
                        us.getSkillType(),
                        us.getProficiencyLevel()
                )
        ).collect(Collectors.toList());
        response.setSkills(skillDtos);

        // Skill DNA: Category counts for proficient skills
        Map<String, Integer> dnaMap = new HashMap<>();
        dnaMap.put("Frontend", 0);
        dnaMap.put("Backend", 0);
        dnaMap.put("Data and AI", 0);
        dnaMap.put("Design", 0);
        dnaMap.put("DevOps", 0);
        dnaMap.put("Mobile", 0);

        for (UserSkill us : skills) {
            String cat = us.getSkill().getCategory();
            dnaMap.put(cat, dnaMap.getOrDefault(cat, 0) + 1);
        }
        response.setSkillDna(dnaMap);

        // Stats
        response.setProjectCount(projectRepository.findByCreatorIdOrderByCreatedAtDesc(user.getId()).size());
        response.setCircleCount(circleMemberRepository.findByUserId(user.getId()).size());
        response.setConnectionCount(connectionRepository.findAcceptedConnections(user.getId()).size());

        return response;
    }

    public List<UserResponse> searchDevelopers(String query, String skillFilter, String collegeFilter, Long currentUserId) {
        List<User> users = userRepository.searchUsers(
                (query != null && !query.isBlank()) ? query.trim() : null,
                (collegeFilter != null && !collegeFilter.isBlank()) ? collegeFilter.trim() : null
        );

        return users.stream()
                .filter(u -> currentUserId == null || !u.getId().equals(currentUserId))
                .filter(u -> {
                    if (skillFilter == null || skillFilter.isBlank()) return true;
                    List<UserSkill> skills = userSkillRepository.findByUserId(u.getId());
                    return skills.stream().anyMatch(s -> s.getSkill().getName().equalsIgnoreCase(skillFilter.trim()));
                })
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }
}
