package com.nexus.controller;

import com.nexus.dto.CollaborationRequestDto;
import com.nexus.dto.ProjectRequest;
import com.nexus.dto.ProjectResponse;
import com.nexus.dto.ProjectUpdateRequest;
import com.nexus.entity.CollaborationRequest;
import com.nexus.entity.User;
import com.nexus.service.ProjectService;
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
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isRescue,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(projectService.getProjects(category, status, isRescue, query));
    }

    @GetMapping("/rescue")
    public ResponseEntity<List<ProjectResponse>> getRescueProjects() {
        return ResponseEntity.ok(projectService.getRescueProjects());
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(projectService.getMyProjects(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectDetails(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectDetails(id));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request,
                                                         Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(projectService.createProject(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id,
                                                         @Valid @RequestBody ProjectRequest request,
                                                         Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(projectService.updateProject(id, user.getId(), request));
    }

    @PostMapping("/{id}/collaborate")
    public ResponseEntity<?> requestCollaboration(@PathVariable Long id,
                                                  @RequestBody(required = false) Map<String, String> body,
                                                  Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        String message = (body != null && body.containsKey("message")) ? body.get("message") : "I would love to contribute to this project!";
        projectService.requestCollaboration(id, user.getId(), message);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Collaboration request sent successfully");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/collaboration-requests/{requestId}/respond")
    public ResponseEntity<?> respondToRequest(@PathVariable Long requestId,
                                              @RequestParam boolean accept,
                                              Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        projectService.respondToCollaborationRequest(requestId, user.getId(), accept);

        Map<String, String> res = new HashMap<>();
        res.put("message", accept ? "Collaboration request accepted" : "Collaboration request declined");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/collaboration-requests/pending")
    public ResponseEntity<List<CollaborationRequestDto>> getPendingRequests(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        List<CollaborationRequest> list = projectService.getPendingRequestsForUser(user.getId());
        List<CollaborationRequestDto> dtos = list.stream().map(r -> new CollaborationRequestDto(
                r.getId(),
                r.getProject().getId(),
                r.getProject().getTitle(),
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

    @PostMapping("/{id}/updates")
    public ResponseEntity<?> addProofOfBuildUpdate(@PathVariable Long id,
                                                   @Valid @RequestBody ProjectUpdateRequest request,
                                                   Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        ProjectResponse.UpdateDto update = projectService.addProofOfBuildUpdate(id, user.getId(), request);
        return ResponseEntity.ok(update);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id,
                                        @RequestBody Map<String, String> body,
                                        Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        String content = body != null ? body.get("content") : null;
        return ResponseEntity.ok(projectService.addComment(id, user.getId(), content));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getComments(id));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long id,
                                        Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(projectService.toggleLike(id, user.getId()));
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<?> getLikes(@PathVariable Long id,
                                      Authentication authentication) {
        long count = projectService.getLikesCount(id);
        boolean liked = false;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                User user = userService.findByEmail(authentication.getName());
                liked = projectService.isLikedByUser(id, user.getId());
            } catch (Exception ignored) {}
        }
        Map<String, Object> res = new HashMap<>();
        res.put("likeCount", count);
        res.put("liked", liked);
        return ResponseEntity.ok(res);
    }
}
