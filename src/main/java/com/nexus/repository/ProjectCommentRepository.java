package com.nexus.repository;

import com.nexus.entity.ProjectComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectCommentRepository extends JpaRepository<ProjectComment, Long> {
    List<ProjectComment> findByProjectIdOrderByCreatedAtAsc(Long projectId);
    long countByProjectId(Long projectId);
    long countByUserId(Long userId);
}
