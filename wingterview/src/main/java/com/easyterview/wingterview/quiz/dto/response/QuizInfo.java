package com.easyterview.wingterview.quiz.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizInfo {
    private final Integer questionIdx;
    private final String question;
    private final String userAnswer;
    private final String correctAnswer;
    private final String commentary;
    private final Boolean isCorrect;
}
