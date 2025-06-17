package com.easyterview.wingterview.interview.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class FeedbackResponseDto {
    @JsonProperty("feedback_lists")
    private List<FeedbackItem> feedbackLists;
}
