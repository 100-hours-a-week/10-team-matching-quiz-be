package com.easyterview.wingterview.interview.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InterviewStatusDto {
    private final String interviewId;
    private final Integer timeRemain;
    private final Integer currentRound;
    private final String currentPhase;
    private final Boolean isInterviewer;
    private final Boolean isAiInterview;
    private final Partner partner;
    private final Integer questionIdx;
    private final String selectedQuestion;
    private final List<String> questionOption;
}
