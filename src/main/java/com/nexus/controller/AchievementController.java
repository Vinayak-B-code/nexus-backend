package com.nexus.controller;

import com.nexus.dto.AchievementRequest;
import com.nexus.dto.AchievementResponse;
import com.nexus.entity.User;
import com.nexus.service.AchievementService;
import com.nexus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementService achievementService;
    private final UserService userService;

    public AchievementController(AchievementService achievementService, UserService userService) {
        this.achievementService = achievementService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<AchievementResponse>> getMyAchievements(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(achievementService.getMyAchievements(user.getId()));
    }

    @GetMapping("/user/{targetUserId}")
    public ResponseEntity<List<AchievementResponse>> getPublicAchievementsForUser(@PathVariable Long targetUserId) {
        return ResponseEntity.ok(achievementService.getPublicAchievementsForUser(targetUserId));
    }

    @PostMapping
    public ResponseEntity<AchievementResponse> addAchievement(@Valid @RequestBody AchievementRequest request,
                                                              Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(achievementService.addAchievement(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AchievementResponse> updateAchievement(@PathVariable Long id,
                                                                 @Valid @RequestBody AchievementRequest request,
                                                                 Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(achievementService.updateAchievement(id, user.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAchievement(@PathVariable Long id,
                                               Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        achievementService.deleteAchievement(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Achievement deleted successfully"));
    }

    @PutMapping("/{id}/visibility")
    public ResponseEntity<AchievementResponse> setVisibility(@PathVariable Long id,
                                                             @RequestParam(name = "public") boolean isPublic,
                                                             Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(achievementService.setVisibility(id, user.getId(), isPublic));
    }
}
