package com.nexus.controller;

import com.nexus.dto.CircleJoinRequestDto;
import com.nexus.dto.CircleRequest;
import com.nexus.dto.CircleResponse;
import com.nexus.dto.MilestoneRequest;
import com.nexus.entity.CircleJoinRequest;
import com.nexus.entity.User;
import com.nexus.service.CircleService;
import com.nexus.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/circles")
public class CircleController {

    private final CircleService circleService;
    private final UserService userService;

    public CircleController(CircleService circleService, UserService userService) {
        this.circleService = circleService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<CircleResponse>> getCircles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(circleService.getCircles(query, status));
    }

    @GetMapping("/my")
    public ResponseEntity<List<CircleResponse>> getMyCircles(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(circleService.getMyCircles(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CircleResponse> getCircleDetails(@PathVariable Long id) {
        return ResponseEntity.ok(circleService.getCircleDetails(id));
    }

    @PostMapping
    public ResponseEntity<CircleResponse> createCircle(@Valid @RequestBody CircleRequest request,
                                                       Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(circleService.createCircle(user.getId(), request));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> requestToJoin(@PathVariable Long id,
                                           @RequestBody(required = false) Map<String, String> body,
                                           Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        String message = (body != null && body.containsKey("message")) ? body.get("message") : "I'm excited to join this circle and help ship the milestones!";
        circleService.requestToJoin(id, user.getId(), message);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Join request submitted successfully");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/join-requests/{requestId}/respond")
    public ResponseEntity<?> respondToJoinRequest(@PathVariable Long requestId,
                                                  @RequestParam boolean accept,
                                                  Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        circleService.respondToJoinRequest(requestId, user.getId(), accept);

        Map<String, String> res = new HashMap<>();
        res.put("message", accept ? "Join request accepted" : "Join request declined");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/join-requests/pending")
    public ResponseEntity<List<CircleJoinRequestDto>> getPendingJoinRequests(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        List<CircleJoinRequest> list = circleService.getPendingRequestsForCreator(user.getId());
        List<CircleJoinRequestDto> dtos = list.stream().map(r -> new CircleJoinRequestDto(
                r.getId(),
                r.getCircle().getId(),
                r.getCircle().getName(),
                r.getRequester().getId(),
                r.getRequester().getFullName(),
                r.getRequester().getCollege(),
                r.getRequester().getAvatarColor(),
                r.getMessage(),
                r.getStatus(),
                r.getCreatedAt()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/milestones")
    public ResponseEntity<CircleResponse.MilestoneDto> addMilestone(@PathVariable Long id,
                                                                    @Valid @RequestBody MilestoneRequest request,
                                                                    Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(circleService.addMilestone(id, user.getId(), request));
    }

    @PutMapping("/milestones/{milestoneId}/toggle")
    public ResponseEntity<CircleResponse.MilestoneDto> toggleMilestone(@PathVariable Long milestoneId,
                                                                       Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(circleService.toggleMilestone(milestoneId, user.getId()));
    }
}
