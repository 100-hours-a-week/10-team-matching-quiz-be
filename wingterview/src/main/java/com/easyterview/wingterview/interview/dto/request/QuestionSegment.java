package com.easyterview.wingterview.interview.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionSegment {
    private String segmentId;
    private Integer startTime;
    private Integer endTime;
    private String question;
}

