package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.ReceivedQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReceivedQuestionRepository extends JpaRepository<ReceivedQuestionEntity, UUID> {
    List<String> findByUserId(UUID userId);
}
