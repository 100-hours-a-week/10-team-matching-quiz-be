package com.easyterview.wingterview.interview.service.interviewflow;

import com.easyterview.wingterview.interview.dto.response.InterviewIdResponse;
import com.easyterview.wingterview.interview.dto.response.NextRoundDto;

public interface InterviewFlowService {
    NextRoundDto goNextStage(String interviewId);

    Object getInterviewStatus();

    void exitInterview(String interviewId);

    InterviewIdResponse getInterviewId(String userId);
}
