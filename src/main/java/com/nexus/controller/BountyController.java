package com.nexus.controller;

import com.nexus.dto.BountyRequest;
import com.nexus.dto.BountyResponseDto;
import com.nexus.entity.User;
import com.nexus.service.BountyService;
import com.nexus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bounties")
public class BountyController {

    private final BountyService bountyService;
    private final UserService userService;

    public BountyController(BountyService bountyService, UserService userService) {
        this.bountyService = bountyService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<BountyResponseDto>> getBounties(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bountyType) {
        return ResponseEntity.ok(bountyService.getBounties(query, status, bountyType));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BountyResponseDto>> getMyBounties(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(bountyService.getMyBounties(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BountyResponseDto> getBountyDetails(@PathVariable Long id) {
        return ResponseEntity.ok(bountyService.getBountyDetails(id));
    }

    @PostMapping
    public ResponseEntity<BountyResponseDto> createBounty(@Valid @RequestBody BountyRequest request,
                                                          Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(bountyService.createBounty(user.getId(), request));
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<?> respondToBounty(@PathVariable Long id,
                                             @RequestBody(required = false) Map<String, String> body,
                                             Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        String message = (body != null && body.containsKey("message")) ? body.get("message") : "I can help resolve this challenge!";
        bountyService.respondToBounty(id, user.getId(), message);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Offer to help submitted successfully");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/responses/{responseId}/handle")
    public ResponseEntity<?> handleResponse(@PathVariable Long responseId,
                                            @RequestParam boolean accept,
                                            Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        bountyService.handleHelperResponse(responseId, user.getId(), accept);

        Map<String, String> res = new HashMap<>();
        res.put("message", accept ? "Helper offer accepted! You are now paired." : "Helper offer declined");
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<BountyResponseDto> markCompleted(@PathVariable Long id,
                                                           Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(bountyService.markCompleted(id, user.getId()));
    }
}
