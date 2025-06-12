package com.easyterview.wingterview.quiz.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class TodayQuizResultRequestDto {
    List<QuizResultItem> quizzes;
}
