package com.easyterview.wingterview.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class InterviewItem {
    private String id;
    private Timestamp createdAt;
    private Long duration;
    private String firstQuestion;
    private Integer questionCount;
    private Boolean isFeedbackRequested;
    private Boolean hasFeedback;
}
