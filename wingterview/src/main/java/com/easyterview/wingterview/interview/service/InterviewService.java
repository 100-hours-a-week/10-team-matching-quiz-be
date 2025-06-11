package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.interview.dto.request.*;
import com.easyterview.wingterview.interview.dto.response.AiInterviewResponseDto;
import com.easyterview.wingterview.interview.dto.response.NextRoundDto;

public interface InterviewService {
    NextRoundDto goNextStage(String interviewId);

    Object getInterviewStatus();

    Object makeQuestion(String interviewId, QuestionCreationRequestDto dto);

    void selectQuestion(String interviewId, QuestionSelectionRequestDto dto);

    void sendFeedback(String interviewId, FeedbackRequestDto dto);

    AiInterviewResponseDto startAiInterview(TimeInitializeRequestDto requestDto);

//    void initializeInterviewTime(String interviewId, TimeInitializeRequestDto dto);

    void exitInterview(String interviewId);

    void getFeedbackFromAI(String userId, FeedbackCallbackDto dto);
}
