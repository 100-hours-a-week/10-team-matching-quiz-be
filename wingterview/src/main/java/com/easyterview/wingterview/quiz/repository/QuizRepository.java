package com.easyterview.wingterview.quiz.repository;

import com.easyterview.wingterview.quiz.entity.QuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<QuizEntity, UUID> {
    List<QuizEntity> findAllByUserId(UUID userId);
}
