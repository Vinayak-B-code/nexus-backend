package com.nexus.repository;

import com.nexus.entity.SkillBounty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillBountyRepository extends JpaRepository<SkillBounty, Long> {
    List<SkillBounty> findByDisabledFalseOrderByCreatedAtDesc();
    List<SkillBounty> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    @Query("SELECT b FROM SkillBounty b WHERE b.disabled = false AND " +
           "(:query IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.skillName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:bountyType IS NULL OR b.bountyType = :bountyType) " +
           "ORDER BY b.createdAt DESC")
    List<SkillBounty> filterBounties(@Param("query") String query,
                                     @Param("status") String status,
                                     @Param("bountyType") String bountyType);
}
