package com.easyterview.wingterview.quiz.service;

import com.easyterview.wingterview.quiz.dto.response.QuizStatsResponse;

public interface QuizService {
    QuizStatsResponse getQuizStats(String userId);
}
