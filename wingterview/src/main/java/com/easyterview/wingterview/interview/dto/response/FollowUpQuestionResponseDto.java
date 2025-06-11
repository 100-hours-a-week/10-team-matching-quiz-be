package com.easyterview.wingterview.interview.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpQuestionResponseDto {

    private String message;

    @JsonProperty("interview_id")
    private String interviewId;

    @JsonProperty("followup_questions")
    private List<String> followupQuestions;
}