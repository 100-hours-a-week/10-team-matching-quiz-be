package com.easyterview.wingterview.user.repository;

import com.easyterview.wingterview.user.entity.InterviewStatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InterviewStatRepository extends JpaRepository<InterviewStatEntity, UUID> {
}
