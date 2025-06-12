package com.easyterview.wingterview.quiz.dto.request;

import lombok.Getter;

@Getter
public class QuizResultItem {
    private Integer quizIdx;
    private Integer userSelection;
    private Boolean isCorrect;
}
