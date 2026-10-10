package com.nexus.repository;

import com.nexus.entity.BountyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BountyResponseRepository extends JpaRepository<BountyResponse, Long> {
    List<BountyResponse> findByBountyId(Long bountyId);
    Optional<BountyResponse> findByBountyIdAndResponderId(Long bountyId, Long responderId);

    @Query("SELECT r FROM BountyResponse r WHERE r.bounty.creator.id = :creatorId AND r.status = 'PENDING' ORDER BY r.createdAt DESC")
    List<BountyResponse> findPendingForCreator(@Param("creatorId") Long creatorId);
}
