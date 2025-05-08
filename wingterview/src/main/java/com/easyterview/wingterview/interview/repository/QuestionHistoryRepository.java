package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.QuestionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionHistoryRepository extends JpaRepository<QuestionHistoryEntity, UUID> {
    Optional<QuestionHistoryEntity> findByInterview(InterviewEntity interview);
}
