package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.QuestionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface QuestionHistoryRepository extends JpaRepository<QuestionHistoryEntity, UUID> {
    Optional<QuestionHistoryEntity> findByInterview(InterviewEntity interview);

    void deleteAllByInterview(InterviewEntity interview);

    @Modifying
    @Transactional
    @Query("DELETE FROM QuestionHistoryEntity q WHERE q.interview.id = :interviewId")
    void deleteAllByInterviewId(@Param("interviewId") UUID interviewId);
}
