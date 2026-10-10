package com.nexus.controller;

import com.nexus.dto.CompatibilityResponse;
import com.nexus.entity.User;
import com.nexus.service.CompatibilityService;
import com.nexus.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compatibility")
public class CompatibilityController {

    private final CompatibilityService compatibilityService;
    private final UserService userService;

    public CompatibilityController(CompatibilityService compatibilityService, UserService userService) {
        this.compatibilityService = compatibilityService;
        this.userService = userService;
    }

    @GetMapping("/{targetUserId}")
    public ResponseEntity<CompatibilityResponse> getCompatibility(@PathVariable Long targetUserId,
                                                                  Authentication authentication) {
        User currentUser = userService.findByEmail(authentication.getName());
        CompatibilityResponse response = compatibilityService.calculateCompatibility(currentUser.getId(), targetUserId);
        return ResponseEntity.ok(response);
    }
}
