package com.easyterview.wingterview.quiz.repository;

import com.easyterview.wingterview.quiz.dto.response.QuizListResponse;

import java.util.UUID;

public interface QuizRepositoryCustom {
    QuizListResponse findByCursorWithLimit(UUID userId, Boolean wrong, UUID cursor, int limit);
}