package com.nexus.service;

import com.nexus.dto.CollaborationRequestDto;
import com.nexus.dto.ProjectCommentDto;
import com.nexus.dto.ProjectRequest;
import com.nexus.dto.ProjectResponse;
import com.nexus.dto.ProjectUpdateRequest;
import com.nexus.entity.CollaborationRequest;
import com.nexus.entity.Project;
import com.nexus.entity.ProjectComment;
import com.nexus.entity.ProjectLike;
import com.nexus.entity.ProjectMember;
import com.nexus.entity.ProjectUpdate;
import com.nexus.entity.User;
import com.nexus.exception.BadRequestException;
import com.nexus.exception.ResourceNotFoundException;
import com.nexus.exception.UnauthorizedException;
import com.nexus.repository.CollaborationRequestRepository;
import com.nexus.repository.ProjectCommentRepository;
import com.nexus.repository.ProjectLikeRepository;
import com.nexus.repository.ProjectMemberRepository;
import com.nexus.repository.ProjectRepository;
import com.nexus.repository.ProjectUpdateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectUpdateRepository projectUpdateRepository;
    private final CollaborationRequestRepository collaborationRequestRepository;
    private final ProjectCommentRepository projectCommentRepository;
    private final ProjectLikeRepository projectLikeRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          ProjectUpdateRepository projectUpdateRepository,
                          CollaborationRequestRepository collaborationRequestRepository,
                          ProjectCommentRepository projectCommentRepository,
                          ProjectLikeRepository projectLikeRepository,
                          UserService userService,
                          NotificationService notificationService,
                          ActivityLogService activityLogService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectUpdateRepository = projectUpdateRepository;
        this.collaborationRequestRepository = collaborationRequestRepository;
        this.projectCommentRepository = projectCommentRepository;
        this.projectLikeRepository = projectLikeRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    public ProjectResponse createProject(Long userId, ProjectRequest request) {
        User user = userService.findById(userId);

        Project project = new Project();
        project.setTitle(request.getTitle().trim());
        project.setShortDescription(request.getShortDescription());
        project.setDetailedDescription(request.getDetailedDescription());
        project.setTechnologies(request.getTechnologies());
        project.setCategory(request.getCategory() != null ? request.getCategory() : "Web");
        project.setGithubUrl(request.getGithubUrl());
        project.setDemoUrl(request.getDemoUrl());
        project.setStatus(request.getStatus() != null ? request.getStatus() : "IN_PROGRESS");
        project.setRescue(request.isRescue());
        project.setRescueBlockers(request.getRescueBlockers());
        project.setRescueSkillsNeeded(request.getRescueSkillsNeeded());
        project.setCreator(user);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        Project saved = projectRepository.save(project);

        // Creator is also lead member
        ProjectMember lead = new ProjectMember(saved, user, "Project Lead");
        projectMemberRepository.save(lead);

        activityLogService.logActivity(user, "PROJECT_CREATE", "Created project: " + saved.getTitle());

        return toProjectResponse(saved);
    }

    public ProjectResponse updateProject(Long projectId, Long userId, ProjectRequest request) {
        Project project = findById(projectId);
        if (!project.getCreator().getId().equals(userId)) {
            throw new UnauthorizedException("Only the project creator can edit this project");
        }

        project.setTitle(request.getTitle().trim());
        project.setShortDescription(request.getShortDescription());
        project.setDetailedDescription(request.getDetailedDescription());
        project.setTechnologies(request.getTechnologies());
        project.setCategory(request.getCategory());
        project.setGithubUrl(request.getGithubUrl());
        project.setDemoUrl(request.getDemoUrl());
        project.setStatus(request.getStatus());
        project.setRescue(request.isRescue());
        project.setRescueBlockers(request.getRescueBlockers());
        project.setRescueSkillsNeeded(request.getRescueSkillsNeeded());
        project.setUpdatedAt(LocalDateTime.now());

        Project saved = projectRepository.save(project);
        activityLogService.logActivity(project.getCreator(), "PROJECT_UPDATE", "Updated project: " + project.getTitle());

        return toProjectResponse(saved);
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
    }

    public ProjectResponse getProjectDetails(Long id) {
        Project project = findById(id);
        if (project.isDisabled()) {
            throw new ResourceNotFoundException("This project has been disabled by moderation");
        }
        return toProjectResponse(project);
    }

    public List<ProjectResponse> getProjects(String category, String status, Boolean isRescue, String query) {
        return projectRepository.filterProjects(
                (category != null && !category.isBlank()) ? category.trim() : null,
                (status != null && !status.isBlank()) ? status.trim() : null,
                isRescue,
                (query != null && !query.isBlank()) ? query.trim() : null
        ).stream().map(this::toProjectResponse).collect(Collectors.toList());
    }

    public List<ProjectResponse> getMyProjects(Long userId) {
        return projectRepository.findByCreatorIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toProjectResponse).collect(Collectors.toList());
    }

    public List<ProjectResponse> getRescueProjects() {
        return projectRepository.findByDisabledFalseAndIsRescueTrueOrderByCreatedAtDesc()
                .stream().map(this::toProjectResponse).collect(Collectors.toList());
    }

    public CollaborationRequest requestCollaboration(Long projectId, Long requesterId, String message) {
        Project project = findById(projectId);
        User requester = userService.findById(requesterId);

        if (project.getCreator().getId().equals(requesterId)) {
            throw new BadRequestException("You are the creator of this project");
        }

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, requesterId)) {
            throw new BadRequestException("You are already a member of this project");
        }

        collaborationRequestRepository.findByProjectIdAndRequesterId(projectId, requesterId)
                .ifPresent(cr -> {
                    if ("PENDING".equals(cr.getStatus())) {
                        throw new BadRequestException("You already have a pending collaboration request for this project");
                    }
                });

        CollaborationRequest req = new CollaborationRequest(project, requester, message);
        CollaborationRequest saved = collaborationRequestRepository.save(req);

        // Notify project owner
        notificationService.createNotification(
                project.getCreator(),
                "Collaboration Request",
                requester.getFullName() + " requested to join your project: " + project.getTitle(),
                "/projects.html?id=" + project.getId()
        );

        activityLogService.logActivity(requester, "PROJECT_COLLAB_REQUEST", "Requested to join project: " + project.getTitle());

        return saved;
    }

    public void respondToCollaborationRequest(Long requestId, Long currentUserId, boolean accept) {
        CollaborationRequest req = collaborationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration request not found"));

        if (!req.getProject().getCreator().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the project creator can respond to collaboration requests");
        }

        req.setStatus(accept ? "ACCEPTED" : "REJECTED");
        collaborationRequestRepository.save(req);

        if (accept) {
            ProjectMember member = new ProjectMember(req.getProject(), req.getRequester(), "Collaborator");
            projectMemberRepository.save(member);
            activityLogService.logActivity(req.getRequester(), "PROJECT_COLLAB_JOINED", "Joined project as collaborator: " + req.getProject().getTitle());
        }

        notificationService.createNotification(
                req.getRequester(),
                accept ? "Collaboration Accepted!" : "Collaboration Request Update",
                accept ? "You are now a collaborator on " + req.getProject().getTitle() :
                         "Your request to join " + req.getProject().getTitle() + " was not accepted",
                "/projects.html?id=" + req.getProject().getId()
        );
    }

    public List<CollaborationRequest> getPendingRequestsForUser(Long userId) {
        return collaborationRequestRepository.findPendingForProjectCreator(userId);
    }

    public ProjectResponse.UpdateDto addProofOfBuildUpdate(Long projectId, Long userId, ProjectUpdateRequest request) {
        Project project = findById(projectId);
        if (!project.getCreator().getId().equals(userId)) {
            throw new UnauthorizedException("Only the project creator can post Proof of Build updates");
        }

        ProjectUpdate update = new ProjectUpdate();
        update.setProject(project);
        update.setTitle(request.getTitle().trim());
        update.setDescription(request.getDescription());
        update.setMilestones(request.getMilestones());
        update.setChallenges(request.getChallenges());
        update.setLessonsLearned(request.getLessonsLearned());
        update.setCreatedAt(LocalDateTime.now());

        ProjectUpdate saved = projectUpdateRepository.save(update);

        activityLogService.logActivity(project.getCreator(), "PROOF_OF_BUILD_UPDATE", "Posted proof of build update for: " + project.getTitle());

        return new ProjectResponse.UpdateDto(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getMilestones(),
                saved.getChallenges(),
                saved.getLessonsLearned(),
                saved.getCreatedAt()
        );
    }

    public ProjectCommentDto addComment(Long projectId, Long userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Comment cannot be empty");
        }
        Project project = findById(projectId);
        User user = userService.findById(userId);

        ProjectComment comment = new ProjectComment(project, user, content.trim());
        ProjectComment saved = projectCommentRepository.save(comment);

        activityLogService.logActivity(user, "PROJECT_COMMENT", "Commented on project: " + project.getTitle());

        if (!project.getCreator().getId().equals(userId)) {
            notificationService.createNotification(
                    project.getCreator(),
                    "New Project Comment",
                    user.getFullName() + " commented on your project: " + project.getTitle(),
                    "/projects.html?id=" + project.getId()
            );
        }

        return toCommentDto(saved);
    }

    public List<ProjectCommentDto> getComments(Long projectId) {
        return projectCommentRepository.findByProjectIdOrderByCreatedAtAsc(projectId)
                .stream().map(this::toCommentDto).collect(Collectors.toList());
    }

    public Map<String, Object> toggleLike(Long projectId, Long userId) {
        Project project = findById(projectId);
        User user = userService.findById(userId);

        Optional<ProjectLike> existing = projectLikeRepository.findByProjectIdAndUserId(projectId, userId);
        boolean liked;
        if (existing.isPresent()) {
            projectLikeRepository.delete(existing.get());
            liked = false;
        } else {
            projectLikeRepository.save(new ProjectLike(project, user));
            liked = true;
            activityLogService.logActivity(user, "PROJECT_LIKE", "Liked project: " + project.getTitle());

            if (!project.getCreator().getId().equals(userId)) {
                notificationService.createNotification(
                        project.getCreator(),
                        "Project Liked",
                        user.getFullName() + " liked your project: " + project.getTitle(),
                        "/projects.html?id=" + project.getId()
                );
            }
        }

        long count = projectLikeRepository.countByProjectId(projectId);
        Map<String, Object> res = new HashMap<>();
        res.put("liked", liked);
        res.put("likeCount", count);
        return res;
    }

    public long getLikesCount(Long projectId) {
        return projectLikeRepository.countByProjectId(projectId);
    }

    public boolean isLikedByUser(Long projectId, Long userId) {
        if (userId == null) return false;
        return projectLikeRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    private ProjectCommentDto toCommentDto(ProjectComment c) {
        return new ProjectCommentDto(
                c.getId(),
                c.getProject().getId(),
                c.getUser().getId(),
                c.getUser().getFullName(),
                c.getUser().getCollege(),
                c.getUser().getAvatarColor(),
                c.getContent(),
                c.getCreatedAt()
        );
    }

    public ProjectResponse toProjectResponse(Project p) {
        ProjectResponse dto = new ProjectResponse();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setShortDescription(p.getShortDescription());
        dto.setDetailedDescription(p.getDetailedDescription());
        dto.setTechnologies(p.getTechnologies());
        dto.setCategory(p.getCategory());
        dto.setGithubUrl(p.getGithubUrl());
        dto.setDemoUrl(p.getDemoUrl());
        dto.setStatus(p.getStatus());
        dto.setRescue(p.isRescue());
        dto.setRescueBlockers(p.getRescueBlockers());
        dto.setRescueSkillsNeeded(p.getRescueSkillsNeeded());
        dto.setDisabled(p.isDisabled());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());

        dto.setCreatorId(p.getCreator().getId());
        dto.setCreatorName(p.getCreator().getFullName());
        dto.setCreatorCollege(p.getCreator().getCollege());
        dto.setCreatorAvatarColor(p.getCreator().getAvatarColor());

        List<ProjectMember> memberEntities = projectMemberRepository.findByProject(p);
        List<ProjectResponse.MemberDto> memberDtos = memberEntities.stream().map(m ->
                new ProjectResponse.MemberDto(
                        m.getId(),
                        m.getUser().getId(),
                        m.getUser().getFullName(),
                        m.getRole(),
                        m.getJoinedAt()
                )
        ).collect(Collectors.toList());
        dto.setMembers(memberDtos);
        dto.setMemberCount(memberDtos.size());

        List<ProjectUpdate> updateEntities = projectUpdateRepository.findByProjectIdOrderByCreatedAtDesc(p.getId());
        List<ProjectResponse.UpdateDto> updateDtos = updateEntities.stream().map(u ->
                new ProjectResponse.UpdateDto(
                        u.getId(),
                        u.getTitle(),
                        u.getDescription(),
                        u.getMilestones(),
                        u.getChallenges(),
                        u.getLessonsLearned(),
                        u.getCreatedAt()
                )
        ).collect(Collectors.toList());
        dto.setUpdates(updateDtos);
        dto.setLikeCount(projectLikeRepository.countByProjectId(p.getId()));
        dto.setCommentCount(projectCommentRepository.countByProjectId(p.getId()));

        return dto;
    }
}
