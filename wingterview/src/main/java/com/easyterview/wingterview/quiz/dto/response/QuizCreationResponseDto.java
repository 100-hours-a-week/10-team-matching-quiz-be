package com.easyterview.wingterview.quiz.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class QuizCreationResponseDto {
    private String interviewId;
    private List<QuizItem> questions;
}
