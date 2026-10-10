package com.nexus.repository;

import com.nexus.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<ActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndActivityType(Long userId, String activityType);

    @Query("SELECT a FROM ActivityLog a WHERE a.user.id = :userId AND a.createdAt >= :startDate AND a.createdAt <= :endDate ORDER BY a.createdAt ASC")
    List<ActivityLog> findActivitiesInRange(@Param("userId") Long userId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a.activityType, COUNT(a) FROM ActivityLog a WHERE a.user.id = :userId AND a.createdAt >= :startDate AND a.createdAt <= :endDate GROUP BY a.activityType")
    List<Object[]> countActivitiesByTypeInRange(@Param("userId") Long userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a.activityType, COUNT(a) FROM ActivityLog a WHERE a.user.id = :userId GROUP BY a.activityType")
    List<Object[]> countActivitiesByType(@Param("userId") Long userId);
}
