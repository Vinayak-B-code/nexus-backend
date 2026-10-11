package com.nexus.controller;

import com.nexus.dto.ProfileUpdateRequest;
import com.nexus.dto.UserResponse;
import com.nexus.entity.User;
import com.nexus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
      
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(userService.toUserResponse(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateMyProfile(@Valid @RequestBody ProfileUpdateRequest request,
                                                        Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        User updated = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(userService.toUserResponse(updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userService.toUserResponse(user));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchDevelopers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String college,
            Authentication authentication) {

        Long currentUserId = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            currentUserId = userService.findByEmail(authentication.getName()).getId();
        }

        List<UserResponse> list = userService.searchDevelopers(query, skill, college, currentUserId);
        return ResponseEntity.ok(list);
    }
}
