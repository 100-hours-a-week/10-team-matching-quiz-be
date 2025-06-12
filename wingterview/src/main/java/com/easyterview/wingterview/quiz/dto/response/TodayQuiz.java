package com.easyterview.wingterview.quiz.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TodayQuiz {
    private final Integer quizIdx;
    private final String question;
    private final List<String> options;
    private final Integer answerIdx;
    private final String commentary;
    private final String difficulty;
}
