package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.QuestionOptionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionOptionsRepository extends JpaRepository<QuestionOptionsEntity, UUID> {

    void deleteAllByInterview(InterviewEntity interview);


    List<QuestionOptionsEntity> findTop4ByInterviewOrderByCreatedAtDesc(InterviewEntity interview);

    List<QuestionOptionsEntity> findTop20ByOrderByCreatedAtDesc();
}
