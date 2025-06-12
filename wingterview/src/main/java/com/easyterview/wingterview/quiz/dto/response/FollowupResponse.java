package com.easyterview.wingterview.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowupResponse {
    private String message;
    private QuizCreationResponseDto data;
}