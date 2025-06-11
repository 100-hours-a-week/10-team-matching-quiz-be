package com.easyterview.wingterview.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizCreationResponseDto {
    private String interviewId;
    private List<QuizItem> questions;
}
