package com.nexus.service;

import com.nexus.dto.CircleRequest;
import com.nexus.dto.CircleResponse;
import com.nexus.dto.MilestoneRequest;
import com.nexus.entity.*;
import com.nexus.exception.BadRequestException;
import com.nexus.exception.ResourceNotFoundException;
import com.nexus.exception.UnauthorizedException;
import com.nexus.repository.BuildCircleRepository;
import com.nexus.repository.CircleJoinRequestRepository;
import com.nexus.repository.CircleMemberRepository;
import com.nexus.repository.CircleMilestoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CircleService {

    private final BuildCircleRepository circleRepository;
    private final CircleMemberRepository memberRepository;
    private final CircleMilestoneRepository milestoneRepository;
    private final CircleJoinRequestRepository joinRequestRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public CircleService(BuildCircleRepository circleRepository,
                         CircleMemberRepository memberRepository,
                         CircleMilestoneRepository milestoneRepository,
                         CircleJoinRequestRepository joinRequestRepository,
                         UserService userService,
                         NotificationService notificationService,
                         ActivityLogService activityLogService) {
        this.circleRepository = circleRepository;
        this.memberRepository = memberRepository;
        this.milestoneRepository = milestoneRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    public CircleResponse createCircle(Long userId, CircleRequest request) {
        User creator = userService.findById(userId);

        BuildCircle circle = new BuildCircle();
        circle.setName(request.getName().trim());
        circle.setDescription(request.getDescription());
        circle.setTechnologies(request.getTechnologies());
        circle.setMaxMembers(Math.max(2, request.getMaxMembers()));
        circle.setOpenPositions(request.getOpenPositions());
        circle.setProjectGoal(request.getProjectGoal());
        circle.setProgressStatus(request.getProgressStatus() != null ? request.getProgressStatus() : "PLANNING");
        circle.setCreator(creator);
        circle.setCreatedAt(LocalDateTime.now());

        BuildCircle saved = circleRepository.save(circle);

        // Creator is first member with CREATOR role
        CircleMember creatorMember = new CircleMember(saved, creator, "CREATOR");
        memberRepository.save(creatorMember);

        activityLogService.logActivity(creator, "CIRCLE_CREATE", "Created Build Circle: " + saved.getName());

        return toCircleResponse(saved);
    }

    public BuildCircle findById(Long id) {
        return circleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Build Circle not found with ID: " + id));
    }

    public CircleResponse getCircleDetails(Long id) {
        BuildCircle circle = findById(id);
        if (circle.isDisabled()) {
            throw new ResourceNotFoundException("This circle has been disabled by moderation");
        }
        return toCircleResponse(circle);
    }

    public List<CircleResponse> getCircles(String query, String status) {
        return circleRepository.filterCircles(
                (query != null && !query.isBlank()) ? query.trim() : null,
                (status != null && !status.isBlank()) ? status.trim() : null
        ).stream().map(this::toCircleResponse).collect(Collectors.toList());
    }

    public List<CircleResponse> getMyCircles(Long userId) {
        List<CircleMember> myMemberships = memberRepository.findByUserId(userId);
        return myMemberships.stream()
                .map(m -> toCircleResponse(m.getCircle()))
                .distinct()
                .collect(Collectors.toList());
    }

    public CircleJoinRequest requestToJoin(Long circleId, Long userId, String message) {
        BuildCircle circle = findById(circleId);
        User requester = userService.findById(userId);

        if (circle.getCreator().getId().equals(userId)) {
            throw new BadRequestException("You are the creator of this Build Circle");
        }

        if (memberRepository.existsByCircleIdAndUserId(circleId, userId)) {
            throw new BadRequestException("You are already a member of this Build Circle");
        }

        long currentCount = memberRepository.countByCircleId(circleId);
        if (currentCount >= circle.getMaxMembers()) {
            throw new BadRequestException("This Build Circle has reached its maximum member capacity (" + circle.getMaxMembers() + ")");
        }

        joinRequestRepository.findByCircleIdAndRequesterId(circleId, userId)
                .ifPresent(req -> {
                    if ("PENDING".equals(req.getStatus())) {
                        throw new BadRequestException("You already have a pending join request for this circle");
                    }
                });

        CircleJoinRequest req = new CircleJoinRequest(circle, requester, message);
        CircleJoinRequest saved = joinRequestRepository.save(req);

        notificationService.createNotification(
                circle.getCreator(),
                "Circle Join Request",
                requester.getFullName() + " requested to join your Build Circle: " + circle.getName(),
                "/circles.html?id=" + circle.getId()
        );

        activityLogService.logActivity(requester, "CIRCLE_JOIN_REQUEST", "Requested to join Build Circle: " + circle.getName());

        return saved;
    }

    public void respondToJoinRequest(Long requestId, Long currentUserId, boolean accept) {
        CircleJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found"));

        BuildCircle circle = req.getCircle();
        if (!circle.getCreator().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the circle creator can accept or reject requests");
        }

        if (accept) {
            long currentCount = memberRepository.countByCircleId(circle.getId());
            if (currentCount >= circle.getMaxMembers()) {
                throw new BadRequestException("Circle has already reached maximum capacity (" + circle.getMaxMembers() + ")");
            }
            CircleMember member = new CircleMember(circle, req.getRequester(), "MEMBER");
            memberRepository.save(member);
            activityLogService.logActivity(req.getRequester(), "CIRCLE_JOINED", "Joined Build Circle: " + circle.getName());
        }

        req.setStatus(accept ? "ACCEPTED" : "REJECTED");
        joinRequestRepository.save(req);

        notificationService.createNotification(
                req.getRequester(),
                accept ? "Welcome to Build Circle!" : "Circle Join Update",
                accept ? "You are now a member of " + circle.getName() : "Your join request for " + circle.getName() + " was not accepted",
                "/circles.html?id=" + circle.getId()
        );
    }

    public List<CircleJoinRequest> getPendingRequestsForCreator(Long creatorId) {
        return joinRequestRepository.findPendingForCreator(creatorId);
    }

    public CircleResponse.MilestoneDto addMilestone(Long circleId, Long userId, MilestoneRequest request) {
        BuildCircle circle = findById(circleId);
        if (!circle.getCreator().getId().equals(userId)) {
            throw new UnauthorizedException("Only the circle creator can add milestones");
        }

        CircleMilestone milestone = new CircleMilestone(circle, request.getTitle().trim(), request.getDescription(), request.getDueDate());
        CircleMilestone saved = milestoneRepository.save(milestone);

        return new CircleResponse.MilestoneDto(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.isCompleted(),
                saved.getDueDate(),
                saved.getCompletedAt()
        );
    }

    public CircleResponse.MilestoneDto toggleMilestone(Long milestoneId, Long userId) {
        CircleMilestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));

        BuildCircle circle = milestone.getCircle();
        if (!circle.getCreator().getId().equals(userId) && !memberRepository.existsByCircleIdAndUserId(circle.getId(), userId)) {
            throw new UnauthorizedException("Only circle members can update milestones");
        }

        milestone.setCompleted(!milestone.isCompleted());
        milestone.setCompletedAt(milestone.isCompleted() ? LocalDateTime.now() : null);
        CircleMilestone saved = milestoneRepository.save(milestone);

        // Notify circle members of progress
        List<CircleMember> members = memberRepository.findByCircle(circle);
        for (CircleMember m : members) {
            if (!m.getUser().getId().equals(userId)) {
                notificationService.createNotification(
                        m.getUser(),
                        "Milestone Updated",
                        "Milestone '" + milestone.getTitle() + "' was marked " + (milestone.isCompleted() ? "complete!" : "in progress"),
                        "/circles.html?id=" + circle.getId()
                );
            }
        }

        return new CircleResponse.MilestoneDto(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.isCompleted(),
                saved.getDueDate(),
                saved.getCompletedAt()
        );
    }

    public CircleResponse toCircleResponse(BuildCircle c) {
        CircleResponse dto = new CircleResponse();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        dto.setTechnologies(c.getTechnologies());
        dto.setMaxMembers(c.getMaxMembers());
        dto.setOpenPositions(c.getOpenPositions());
        dto.setProjectGoal(c.getProjectGoal());
        dto.setProgressStatus(c.getProgressStatus());
        dto.setDisabled(c.isDisabled());
        dto.setCreatedAt(c.getCreatedAt());

        dto.setCreatorId(c.getCreator().getId());
        dto.setCreatorName(c.getCreator().getFullName());
        dto.setCreatorCollege(c.getCreator().getCollege());
        dto.setCreatorAvatarColor(c.getCreator().getAvatarColor());

        List<CircleMember> memberEntities = memberRepository.findByCircle(c);
        List<CircleResponse.CircleMemberDto> memberDtos = memberEntities.stream().map(m ->
                new CircleResponse.CircleMemberDto(
                        m.getId(),
                        m.getUser().getId(),
                        m.getUser().getFullName(),
                        m.getRole(),
                        m.getJoinedAt()
                )
        ).collect(Collectors.toList());
        dto.setMembers(memberDtos);
        dto.setCurrentMemberCount(memberDtos.size());

        List<CircleMilestone> milestoneEntities = milestoneRepository.findByCircleIdOrderByDueDateAsc(c.getId());
        List<CircleResponse.MilestoneDto> milestoneDtos = milestoneEntities.stream().map(m ->
                new CircleResponse.MilestoneDto(
                        m.getId(),
                        m.getTitle(),
                        m.getDescription(),
                        m.isCompleted(),
                        m.getDueDate(),
                        m.getCompletedAt()
                )
        ).collect(Collectors.toList());
        dto.setMilestones(milestoneDtos);
        dto.setTotalMilestonesCount(milestoneDtos.size());
        int completedCount = (int) milestoneDtos.stream().filter(CircleResponse.MilestoneDto::isCompleted).count();
        dto.setCompletedMilestonesCount(completedCount);
        int percentage = milestoneDtos.isEmpty() ? 0 : (int) Math.round(((double) completedCount / milestoneDtos.size()) * 100);
        dto.setProgressPercentage(percentage);

        return dto;
    }
}
