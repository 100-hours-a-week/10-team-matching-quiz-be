package com.easyterview.wingterview.quiz.service;

import com.easyterview.wingterview.quiz.dto.response.QuizListResponse;
import com.easyterview.wingterview.quiz.dto.response.QuizStatsResponse;
import com.easyterview.wingterview.quiz.dto.response.TodayQuizListResponse;

public interface QuizService {
    QuizStatsResponse getQuizStats(String userId);

    QuizListResponse getQuizList(String userId, Boolean wrong, String cursor, Integer limit);

    TodayQuizListResponse getTodayQuiz(String userId);

    void createTodayQuiz();
}
