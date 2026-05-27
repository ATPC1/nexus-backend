package com.nexustalk.backend.repository;

import com.nexustalk.backend.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByGroupIdOrderByUploadedAtDesc(Long groupId);
}
