package com.easyterview.wingterview.interview.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class FeedbackResponseDto {
    private List<FeedbackItem> feedbackLists;
}
