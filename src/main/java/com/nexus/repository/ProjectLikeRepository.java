package com.nexus.repository;

import com.nexus.entity.ProjectLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectLikeRepository extends JpaRepository<ProjectLike, Long> {
    Optional<ProjectLike> findByProjectIdAndUserId(Long projectId, Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    long countByProjectId(Long projectId);
    long countByUserId(Long userId);
    void deleteByProjectIdAndUserId(Long projectId, Long userId);
}
