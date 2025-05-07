package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.interview.dto.response.InterviewStatusDto;
import com.easyterview.wingterview.interview.dto.response.NextRoundDto;

public interface InterviewService {
    NextRoundDto goNextStage(String interviewId);

    InterviewStatusDto getInterviewStatus();
}
