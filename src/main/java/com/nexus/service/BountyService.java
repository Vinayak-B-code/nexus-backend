package com.nexus.service;

import com.nexus.dto.BountyRequest;
import com.nexus.dto.BountyResponseDto;
import com.nexus.entity.BountyResponse;
import com.nexus.entity.SkillBounty;
import com.nexus.entity.User;
import com.nexus.exception.BadRequestException;
import com.nexus.exception.ResourceNotFoundException;
import com.nexus.exception.UnauthorizedException;
import com.nexus.repository.BountyResponseRepository;
import com.nexus.repository.SkillBountyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BountyService {

    private final SkillBountyRepository bountyRepository;
    private final BountyResponseRepository responseRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public BountyService(SkillBountyRepository bountyRepository,
                         BountyResponseRepository responseRepository,
                         UserService userService,
                         NotificationService notificationService,
                         ActivityLogService activityLogService) {
        this.bountyRepository = bountyRepository;
        this.responseRepository = responseRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    public BountyResponseDto createBounty(Long userId, BountyRequest request) {
        User creator = userService.findById(userId);

        SkillBounty bounty = new SkillBounty();
        bounty.setTitle(request.getTitle().trim());
        bounty.setDescription(request.getDescription().trim());
        bounty.setSkillName(request.getSkillName().trim());
        bounty.setBountyType(request.getBountyType() != null ? request.getBountyType() : "DEBUGGING");
        bounty.setStatus("OPEN");
        bounty.setCreator(creator);
        bounty.setCreatedAt(LocalDateTime.now());

        SkillBounty saved = bountyRepository.save(bounty);
        activityLogService.logActivity(creator, "BOUNTY_CREATE", "Posted skill bounty: " + saved.getTitle());

        return toDto(saved);
    }

    public SkillBounty findById(Long id) {
        return bountyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill Bounty not found with ID: " + id));
    }

    public BountyResponseDto getBountyDetails(Long id) {
        SkillBounty b = findById(id);
        if (b.isDisabled()) {
            throw new ResourceNotFoundException("This bounty has been removed by moderation");
        }
        return toDto(b);
    }

    public List<BountyResponseDto> getBounties(String query, String status, String bountyType) {
        return bountyRepository.filterBounties(
                (query != null && !query.isBlank()) ? query.trim() : null,
                (status != null && !status.isBlank()) ? status.trim() : null,
                (bountyType != null && !bountyType.isBlank()) ? bountyType.trim() : null
        ).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<BountyResponseDto> getMyBounties(Long userId) {
        return bountyRepository.findByCreatorIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public BountyResponse respondToBounty(Long bountyId, Long userId, String message) {
        SkillBounty bounty = findById(bountyId);
        User responder = userService.findById(userId);

        if (bounty.getCreator().getId().equals(userId)) {
            throw new BadRequestException("You cannot claim or respond to your own bounty");
        }

        if (!"OPEN".equals(bounty.getStatus())) {
            throw new BadRequestException("This bounty is no longer open for responses");
        }

        responseRepository.findByBountyIdAndResponderId(bountyId, userId)
                .ifPresent(r -> {
                    if ("PENDING".equals(r.getStatus())) {
                        throw new BadRequestException("You have already submitted an offer to help with this bounty");
                    }
                });

        BountyResponse resp = new BountyResponse(bounty, responder, message);
        BountyResponse saved = responseRepository.save(resp);

        notificationService.createNotification(
                bounty.getCreator(),
                "Helper Offered on Bounty",
                responder.getFullName() + " offered to help with your bounty: " + bounty.getTitle(),
                "/bounties.html?id=" + bounty.getId()
        );

        activityLogService.logActivity(responder, "BOUNTY_CLAIM", "Offered help on bounty: " + bounty.getTitle());

        return saved;
    }

    public void handleHelperResponse(Long responseId, Long creatorId, boolean accept) {
        BountyResponse resp = responseRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("Helper response not found"));

        SkillBounty bounty = resp.getBounty();
        if (!bounty.getCreator().getId().equals(creatorId)) {
            throw new UnauthorizedException("Only the bounty author can accept or decline helpers");
        }

        resp.setStatus(accept ? "ACCEPTED" : "REJECTED");
        responseRepository.save(resp);

        if (accept) {
            bounty.setStatus("IN_PROGRESS");
            bounty.setAcceptedHelper(resp.getResponder());
            bountyRepository.save(bounty);
            activityLogService.logActivity(resp.getResponder(), "BOUNTY_ACCEPTED", "Helper offer accepted for: " + bounty.getTitle());
        }

        notificationService.createNotification(
                resp.getResponder(),
                accept ? "Bounty Offer Accepted!" : "Bounty Offer Update",
                accept ? "Your offer to help with '" + bounty.getTitle() + "' was accepted! You are now paired." :
                         "Your offer for '" + bounty.getTitle() + "' was declined.",
                "/bounties.html?id=" + bounty.getId()
        );
    }

    public BountyResponseDto markCompleted(Long bountyId, Long userId) {
        SkillBounty bounty = findById(bountyId);
        if (!bounty.getCreator().getId().equals(userId)) {
            throw new UnauthorizedException("Only the bounty author can mark the bounty as completed");
        }

        bounty.setStatus("COMPLETED");
        bounty.setCompletedAt(LocalDateTime.now());
        SkillBounty saved = bountyRepository.save(bounty);

        activityLogService.logActivity(bounty.getCreator(), "BOUNTY_COMPLETED", "Completed bounty: " + bounty.getTitle());

        if (bounty.getAcceptedHelper() != null) {
            activityLogService.logActivity(bounty.getAcceptedHelper(), "BOUNTY_SOLVED", "Successfully solved bounty: " + bounty.getTitle());
            notificationService.createNotification(
                    bounty.getAcceptedHelper(),
                    "Bounty Completed!",
                    "Bounty '" + bounty.getTitle() + "' has been marked successfully completed! Kudos for helping.",
                    "/bounties.html?id=" + bounty.getId()
            );
        }

        return toDto(saved);
    }

    public BountyResponseDto toDto(SkillBounty b) {
        BountyResponseDto dto = new BountyResponseDto();
        dto.setId(b.getId());
        dto.setTitle(b.getTitle());
        dto.setDescription(b.getDescription());
        dto.setSkillName(b.getSkillName());
        dto.setBountyType(b.getBountyType());
        dto.setStatus(b.getStatus());
        dto.setDisabled(b.isDisabled());
        dto.setCreatedAt(b.getCreatedAt());
        dto.setCompletedAt(b.getCompletedAt());

        dto.setCreatorId(b.getCreator().getId());
        dto.setCreatorName(b.getCreator().getFullName());
        dto.setCreatorCollege(b.getCreator().getCollege());
        dto.setCreatorAvatarColor(b.getCreator().getAvatarColor());

        if (b.getAcceptedHelper() != null) {
            dto.setAcceptedHelperId(b.getAcceptedHelper().getId());
            dto.setAcceptedHelperName(b.getAcceptedHelper().getFullName());
        }

        List<BountyResponse> responses = responseRepository.findByBountyId(b.getId());
        List<BountyResponseDto.HelperOfferDto> offerDtos = responses.stream().map(r ->
                new BountyResponseDto.HelperOfferDto(
                        r.getId(),
                        r.getResponder().getId(),
                        r.getResponder().getFullName(),
                        r.getResponder().getCollege(),
                        r.getResponder().getAvatarColor(),
                        r.getMessage(),
                        r.getStatus(),
                        r.getCreatedAt()
                )
        ).collect(Collectors.toList());
        dto.setResponses(offerDtos);
        dto.setResponseCount(offerDtos.size());

        return dto;
    }
}
