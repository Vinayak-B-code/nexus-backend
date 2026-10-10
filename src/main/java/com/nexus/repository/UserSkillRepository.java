package com.nexus.repository;

import com.nexus.entity.User;
import com.nexus.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUser(User user);
    List<UserSkill> findByUserId(Long userId);
    Optional<UserSkill> findByUserIdAndSkillIdAndSkillType(Long userId, Long skillId, String skillType);
    void deleteByUserIdAndSkillId(Long userId, Long skillId);
}
