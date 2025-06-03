package com.easyterview.wingterview.quiz.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizStatsResponse {
    private final float correctRate;
}
