package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.QuestionOptionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionOptionsRepository extends JpaRepository<QuestionOptionsEntity, UUID> {

    Optional<QuestionOptionsEntity> findTop1ByInterviewOrderByCreatedAtDesc(InterviewEntity interview);

    void deleteAllByInterview(InterviewEntity interview);


    List<QuestionOptionsEntity> findTop5ByOrderByCreatedAtDesc();
}
