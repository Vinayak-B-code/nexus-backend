package com.nexus.controller;

import com.nexus.dto.StatsResponse;
import com.nexus.dto.UserResponse;
import com.nexus.service.AdminService;
import com.nexus.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @GetMapping("/api/stats/public")
    public ResponseEntity<StatsResponse> getPublicStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/api/admin/stats")
    public ResponseEntity<StatsResponse> getAdminStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/api/admin/users")
    public ResponseEntity<List<UserResponse>> getAdminUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/api/admin/projects/{id}/toggle-status")
    public ResponseEntity<?> toggleProject(@PathVariable Long id, @RequestParam boolean disabled) {
        adminService.toggleProjectStatus(id, disabled);
        return ResponseEntity.ok(Map.of("message", "Project status updated"));
    }

    @PutMapping("/api/admin/circles/{id}/toggle-status")
    public ResponseEntity<?> toggleCircle(@PathVariable Long id, @RequestParam boolean disabled) {
        adminService.toggleCircleStatus(id, disabled);
        return ResponseEntity.ok(Map.of("message", "Circle status updated"));
    }

    @PutMapping("/api/admin/bounties/{id}/toggle-status")
    public ResponseEntity<?> toggleBounty(@PathVariable Long id, @RequestParam boolean disabled) {
        adminService.toggleBountyStatus(id, disabled);
        return ResponseEntity.ok(Map.of("message", "Bounty status updated"));
    }
}
