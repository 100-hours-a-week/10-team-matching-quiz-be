package com.easyterview.wingterview.quiz.repository;

import com.easyterview.wingterview.quiz.entity.QuizSelectionEntity;
import com.easyterview.wingterview.quiz.entity.TodayQuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizSelectionRepository extends JpaRepository<QuizSelectionEntity, UUID> {
    List<QuizSelectionEntity> findAllByTodayQuiz(TodayQuizEntity e);
}
