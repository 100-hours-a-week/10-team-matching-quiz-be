package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.ReceivedQuestionEntity;
import com.querydsl.core.Fetchable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReceivedQuestionRepository extends JpaRepository<ReceivedQuestionEntity, UUID> {
    List<ReceivedQuestionEntity> findTop10ByUserIdOrderByReceivedAt(UUID userId);
}
