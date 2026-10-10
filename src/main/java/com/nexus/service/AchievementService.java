package com.nexus.service;

import com.nexus.dto.AchievementRequest;
import com.nexus.dto.AchievementResponse;
import com.nexus.entity.Achievement;
import com.nexus.entity.User;
import com.nexus.exception.BadRequestException;
import com.nexus.exception.ResourceNotFoundException;
import com.nexus.exception.UnauthorizedException;
import com.nexus.repository.AchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserService userService;
    private final ActivityLogService activityLogService;

    public static final Set<String> VALID_CATEGORIES = Set.of(
            "Hackathons",
            "Coding competitions",
            "Certifications",
            "Internships",
            "Workshops",
            "Academic accomplishments",
            "Personal projects",
            "Other"
    );

    public AchievementService(AchievementRepository achievementRepository,
                              UserService userService,
                              ActivityLogService activityLogService) {
        this.achievementRepository = achievementRepository;
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    public AchievementResponse addAchievement(Long userId, AchievementRequest request) {
        User user = userService.findById(userId);
        validateRequest(request);

        Achievement achievement = new Achievement();
        achievement.setUser(user);
        achievement.setTitle(request.getTitle().trim());
        achievement.setCategory(normalizeCategory(request.getCategory()));
        achievement.setDescription(request.getDescription());
        achievement.setOrganization(request.getOrganization() != null ? request.getOrganization().trim() : null);
        achievement.setAchievementDate(request.getAchievementDate());
        achievement.setProofUrl(sanitizeUrl(request.getProofUrl()));
        achievement.setVerificationStatus("SELF_REPORTED");
        achievement.setPublic(request.isPublic());
        achievement.setCreatedAt(LocalDateTime.now());
        achievement.setUpdatedAt(LocalDateTime.now());

        Achievement saved = achievementRepository.save(achievement);

        activityLogService.logActivity(
                user,
                "ACHIEVEMENT_CREATE",
                "Added academic achievement: " + saved.getTitle() + " (" + saved.getCategory() + ")"
        );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getMyAchievements(Long userId) {
        return achievementRepository.findByUserIdOrderByAchievementDateDescCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getPublicAchievementsForUser(Long targetUserId) {
        return achievementRepository.findByUserIdAndIsPublicTrueOrderByAchievementDateDescCreatedAtDesc(targetUserId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AchievementResponse updateAchievement(Long achievementId, Long userId, AchievementRequest request) {
        Achievement achievement = findById(achievementId);
        if (!achievement.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to edit this achievement");
        }
        validateRequest(request);

        achievement.setTitle(request.getTitle().trim());
        achievement.setCategory(normalizeCategory(request.getCategory()));
        achievement.setDescription(request.getDescription());
        achievement.setOrganization(request.getOrganization() != null ? request.getOrganization().trim() : null);
        achievement.setAchievementDate(request.getAchievementDate());
        achievement.setProofUrl(sanitizeUrl(request.getProofUrl()));
        achievement.setPublic(request.isPublic());
        achievement.setUpdatedAt(LocalDateTime.now());

        Achievement saved = achievementRepository.save(achievement);

        activityLogService.logActivity(
                achievement.getUser(),
                "ACHIEVEMENT_UPDATE",
                "Updated achievement details: " + saved.getTitle()
        );

        return toResponse(saved);
    }

    public void deleteAchievement(Long achievementId, Long userId) {
        Achievement achievement = findById(achievementId);
        if (!achievement.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this achievement");
        }

        String title = achievement.getTitle();
        achievementRepository.delete(achievement);

        activityLogService.logActivity(
                achievement.getUser(),
                "ACHIEVEMENT_DELETE",
                "Removed achievement: " + title
        );
    }

    public AchievementResponse setVisibility(Long achievementId, Long userId, boolean isPublic) {
        Achievement achievement = findById(achievementId);
        if (!achievement.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to change this achievement's visibility");
        }

        achievement.setPublic(isPublic);
        achievement.setUpdatedAt(LocalDateTime.now());
        Achievement saved = achievementRepository.save(achievement);

        activityLogService.logActivity(
                achievement.getUser(),
                "ACHIEVEMENT_VISIBILITY",
                "Changed achievement visibility to " + (isPublic ? "Public" : "Private") + ": " + saved.getTitle()
        );

        return toResponse(saved);
    }

    public Achievement findById(Long id) {
        return achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with ID: " + id));
    }

    private void validateRequest(AchievementRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isBlank()) {
            throw new BadRequestException("Achievement title is required");
        }
        if (request.getCategory() == null || request.getCategory().trim().isBlank()) {
            throw new BadRequestException("Achievement category is required");
        }
        normalizeCategory(request.getCategory());
        sanitizeUrl(request.getProofUrl());
    }

    private String normalizeCategory(String input) {
        if (input == null) return "Other";
        String trimmed = input.trim();
        for (String valid : VALID_CATEGORIES) {
            if (valid.equalsIgnoreCase(trimmed)) {
                return valid;
            }
        }
        throw new BadRequestException("Invalid category: " + input + ". Allowed categories: " + String.join(", ", VALID_CATEGORIES));
    }

    private String sanitizeUrl(String url) {
        if (url == null || url.trim().isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        try {
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new BadRequestException("Invalid URL: Only http:// or https:// URLs are permitted");
            }
            return trimmed;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid proof URL format: " + e.getMessage());
        }
    }

    public AchievementResponse toResponse(Achievement a) {
        AchievementResponse dto = new AchievementResponse();
        dto.setId(a.getId());
        dto.setUserId(a.getUser().getId());
        dto.setUserName(a.getUser().getFullName());
        dto.setUserAvatarColor(a.getUser().getAvatarColor());
        dto.setTitle(a.getTitle());
        dto.setCategory(a.getCategory());
        dto.setDescription(a.getDescription());
        dto.setOrganization(a.getOrganization());
        dto.setAchievementDate(a.getAchievementDate());
        dto.setProofUrl(a.getProofUrl());
        dto.setVerificationStatus(a.getVerificationStatus());
        dto.setPublic(a.isPublic());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }
}
