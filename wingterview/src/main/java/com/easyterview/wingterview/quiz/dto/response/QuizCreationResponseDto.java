package com.easyterview.wingterview.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
@ToString
public class QuizCreationResponseDto {

    private String interviewId;
    private List<QuizItem> questions;

    @JsonCreator
    public QuizCreationResponseDto(
            @JsonProperty("interview_id") String interviewId,
            @JsonProperty("questions") List<QuizItem> questions) {
        this.interviewId = interviewId;
        this.questions = questions;
    }
}
