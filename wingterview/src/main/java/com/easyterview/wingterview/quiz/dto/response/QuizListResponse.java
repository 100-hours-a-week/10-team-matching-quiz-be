package com.easyterview.wingterview.quiz.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuizListResponse {
    private List<QuizInfo> quizInfoList;
    private Boolean hasNext;
    private Integer nextCursor;
}
