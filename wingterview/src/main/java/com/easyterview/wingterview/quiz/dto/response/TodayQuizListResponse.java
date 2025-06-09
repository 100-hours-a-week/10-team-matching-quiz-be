package com.easyterview.wingterview.quiz.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TodayQuizListResponse {
    private final List<TodayQuiz> quizList;
}
