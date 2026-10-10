package com.nexus.repository;

import com.nexus.entity.BuildCircle;
import com.nexus.entity.CircleMember;
import com.nexus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CircleMemberRepository extends JpaRepository<CircleMember, Long> {
    List<CircleMember> findByCircle(BuildCircle circle);
    List<CircleMember> findByCircleId(Long circleId);
    Optional<CircleMember> findByCircleIdAndUserId(Long circleId, Long userId);
    boolean existsByCircleIdAndUserId(Long circleId, Long userId);
    long countByCircleId(Long circleId);
    List<CircleMember> findByUserId(Long userId);
}
