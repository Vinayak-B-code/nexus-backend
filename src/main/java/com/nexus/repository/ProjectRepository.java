package com.nexus.repository;

import com.nexus.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByDisabledFalseOrderByCreatedAtDesc();
    List<Project> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
    List<Project> findByDisabledFalseAndIsRescueTrueOrderByCreatedAtDesc();

    @Query("SELECT p FROM Project p WHERE p.disabled = false AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:isRescue IS NULL OR p.isRescue = :isRescue) AND " +
           "(:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.technologies) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY p.createdAt DESC")
    List<Project> filterProjects(@Param("category") String category,
                                 @Param("status") String status,
                                 @Param("isRescue") Boolean isRescue,
                                 @Param("query") String query);
}
