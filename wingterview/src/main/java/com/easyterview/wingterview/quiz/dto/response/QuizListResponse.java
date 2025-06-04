package com.easyterview.wingterview.quiz.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class QuizListResponse {
    private final List<QuizInfo> quizInfoList;
    private final Boolean hasNext;
    private final String nextCursor;
}
