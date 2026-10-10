package com.nexus.repository;

import com.nexus.entity.BuildCircle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildCircleRepository extends JpaRepository<BuildCircle, Long> {
    List<BuildCircle> findByDisabledFalseOrderByCreatedAtDesc();
    List<BuildCircle> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    @Query("SELECT c FROM BuildCircle c WHERE c.disabled = false AND " +
           "(:query IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.technologies) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR c.progressStatus = :status) " +
           "ORDER BY c.createdAt DESC")
    List<BuildCircle> filterCircles(@Param("query") String query, @Param("status") String status);
}
