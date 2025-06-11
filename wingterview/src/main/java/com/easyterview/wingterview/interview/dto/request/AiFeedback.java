package com.easyterview.wingterview.interview.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class AiFeedback {

    private String question;

    @JsonProperty("user_answer")
    private String userAnswer;

    @JsonProperty("expected_answer")
    private String expectedAnswer;

    private String feedback;
}
