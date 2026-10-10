package com.nexus.repository;

import com.nexus.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByUserIdOrderByAchievementDateDescCreatedAtDesc(Long userId);

    List<Achievement> findByUserIdAndIsPublicTrueOrderByAchievementDateDescCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndIsPublicTrue(Long userId);
}
