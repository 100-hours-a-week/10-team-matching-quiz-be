package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.InterviewFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedbackEntity, UUID> {
}
