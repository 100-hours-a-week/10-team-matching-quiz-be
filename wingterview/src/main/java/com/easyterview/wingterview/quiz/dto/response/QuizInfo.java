package com.easyterview.wingterview.quiz.dto.response;

import com.easyterview.wingterview.quiz.entity.QuizEntity;
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

    public static QuizInfo fromEntity(QuizEntity entity, Integer questionIdx) {
        return QuizInfo.builder()
                .questionIdx(questionIdx)
                .question(entity.getQuestion())
                .commentary(entity.getCommentary())
                .userAnswer(entity.getUserAnswer())
                .correctAnswer(entity.getCorrectAnswer())
                .isCorrect(entity.getIsCorrect())
                .build();
    }
}