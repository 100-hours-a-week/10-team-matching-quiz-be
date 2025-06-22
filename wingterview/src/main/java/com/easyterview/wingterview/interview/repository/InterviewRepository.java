package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.InterviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InterviewRepository extends JpaRepository<InterviewEntity, UUID> {

}
