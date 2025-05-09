package com.easyterview.wingterview.interview.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class FollowUpQuestionRequest {

    @JsonProperty("interview_id")
    private String interviewId;

    @JsonProperty("selected_question")
    private String selectedQuestion;

    @JsonProperty("keyword")
    private String keyword;

    @JsonProperty("passed_questions")
    private List<String> passedQuestions;
}