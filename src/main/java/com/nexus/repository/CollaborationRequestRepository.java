package com.nexus.repository;

import com.nexus.entity.CollaborationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollaborationRequestRepository extends JpaRepository<CollaborationRequest, Long> {
    List<CollaborationRequest> findByProjectId(Long projectId);
    Optional<CollaborationRequest> findByProjectIdAndRequesterId(Long projectId, Long requesterId);

    @Query("SELECT cr FROM CollaborationRequest cr WHERE cr.project.creator.id = :userId AND cr.status = 'PENDING' ORDER BY cr.createdAt DESC")
    List<CollaborationRequest> findPendingForProjectCreator(@Param("userId") Long userId);

    List<CollaborationRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
}
