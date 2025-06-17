package com.easyterview.wingterview.quiz.repository;

import com.easyterview.wingterview.quiz.entity.QuizGenerationStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface QuizGenerationStatusRepository extends JpaRepository<QuizGenerationStatusEntity, Integer> {
    QuizGenerationStatusEntity findTopByOrderByStartAtDesc();
}
