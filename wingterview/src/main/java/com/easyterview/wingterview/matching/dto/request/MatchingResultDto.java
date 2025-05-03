package com.easyterview.wingterview.matching.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchingResultDto {
    private final Boolean isFirstInterviewer;
    private final Boolean isAiInterview;
    private final Interviewer interviewer;
    private final Interviewee interviewee;
    private final String interviewId;
}
