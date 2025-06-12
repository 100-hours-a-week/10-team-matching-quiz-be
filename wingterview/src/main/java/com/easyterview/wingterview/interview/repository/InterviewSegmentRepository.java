package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.interview.entity.InterviewSegmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InterviewSegmentRepository extends JpaRepository<InterviewSegmentEntity, UUID> {
    Integer countByInterviewHistory(InterviewHistoryEntity interviewHistory);
}
