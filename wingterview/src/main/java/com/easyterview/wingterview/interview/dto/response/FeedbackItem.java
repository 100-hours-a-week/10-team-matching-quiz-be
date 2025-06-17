package com.easyterview.wingterview.interview.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class FeedbackItem {
    private String segmentId;
    private String modelAnswer;
    private String feedback;
}
