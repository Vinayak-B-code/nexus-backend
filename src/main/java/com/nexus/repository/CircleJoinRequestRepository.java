package com.nexus.repository;

import com.nexus.entity.CircleJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CircleJoinRequestRepository extends JpaRepository<CircleJoinRequest, Long> {
    List<CircleJoinRequest> findByCircleId(Long circleId);
    Optional<CircleJoinRequest> findByCircleIdAndRequesterId(Long circleId, Long requesterId);

    @Query("SELECT r FROM CircleJoinRequest r WHERE r.circle.creator.id = :creatorId AND r.status = 'PENDING' ORDER BY r.createdAt DESC")
    List<CircleJoinRequest> findPendingForCreator(@Param("creatorId") Long creatorId);
}
