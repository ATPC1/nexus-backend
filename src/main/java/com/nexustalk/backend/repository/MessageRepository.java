package com.nexustalk.backend.repository;

import com.nexustalk.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByGroupIdOrderByTimestampAsc(Long groupId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE m.group.id = :groupId")
    void deleteByGroupId(Long groupId);
}
