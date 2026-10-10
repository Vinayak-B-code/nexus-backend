package com.nexus.repository;

import com.nexus.entity.CircleMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CircleMilestoneRepository extends JpaRepository<CircleMilestone, Long> {
    List<CircleMilestone> findByCircleIdOrderByDueDateAsc(Long circleId);
    long countByCircleId(Long circleId);
    long countByCircleIdAndCompletedTrue(Long circleId);
}
