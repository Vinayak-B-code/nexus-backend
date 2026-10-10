package com.nexus.service;

import com.nexus.dto.StatsResponse;
import com.nexus.entity.BuildCircle;
import com.nexus.entity.Project;
import com.nexus.entity.SkillBounty;
import com.nexus.exception.ResourceNotFoundException;
import com.nexus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final BuildCircleRepository circleRepository;
    private final SkillBountyRepository bountyRepository;
    private final ConnectionRepository connectionRepository;

    public AdminService(UserRepository userRepository,
                        ProjectRepository projectRepository,
                        BuildCircleRepository circleRepository,
                        SkillBountyRepository bountyRepository,
                        ConnectionRepository connectionRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.circleRepository = circleRepository;
        this.bountyRepository = bountyRepository;
        this.connectionRepository = connectionRepository;
    }

    public StatsResponse getStats() {
        long users = userRepository.count();
        long projects = projectRepository.count();
        long rescueProjects = projectRepository.findByDisabledFalseAndIsRescueTrueOrderByCreatedAtDesc().size();
        long circles = circleRepository.count();
        long bounties = bountyRepository.count();
        long connections = connectionRepository.count();

        return new StatsResponse(users, projects, rescueProjects, circles, bounties, connections);
    }

    public void toggleProjectStatus(Long projectId, boolean disabled) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        p.setDisabled(disabled);
        projectRepository.save(p);
    }

    public void toggleCircleStatus(Long circleId, boolean disabled) {
        BuildCircle c = circleRepository.findById(circleId)
                .orElseThrow(() -> new ResourceNotFoundException("Circle not found"));
        c.setDisabled(disabled);
        circleRepository.save(c);
    }

    public void toggleBountyStatus(Long bountyId, boolean disabled) {
        SkillBounty b = bountyRepository.findById(bountyId)
                .orElseThrow(() -> new ResourceNotFoundException("Bounty not found"));
        b.setDisabled(disabled);
        bountyRepository.save(b);
    }
}
