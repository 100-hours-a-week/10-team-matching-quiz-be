package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InterviewHistoryRepository extends JpaRepository<InterviewHistoryEntity, UUID> {
}
