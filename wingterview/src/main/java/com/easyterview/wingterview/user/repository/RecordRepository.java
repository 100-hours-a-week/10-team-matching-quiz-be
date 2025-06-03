package com.easyterview.wingterview.user.repository;

import com.easyterview.wingterview.user.entity.RecordingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecordRepository extends JpaRepository<RecordingEntity, UUID> {
    void deleteByUrl(String url);

    RecordingEntity findByInterviewId(UUID interviewId);
}
