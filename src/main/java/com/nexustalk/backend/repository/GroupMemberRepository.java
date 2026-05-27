package com.nexustalk.backend.repository;

import com.nexustalk.backend.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByUserId(Long userId);
    List<GroupMember> findByGroupId(Long groupId);
    boolean existsByUserIdAndGroupId(Long userId, Long groupId);
    Optional<GroupMember> findByUserIdAndGroupId(Long userId, Long groupId);
}
