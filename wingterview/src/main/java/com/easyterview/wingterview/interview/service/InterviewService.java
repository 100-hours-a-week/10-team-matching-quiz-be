package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.interview.dto.request.FeedbackRequestDto;
import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.dto.request.QuestionSelectionRequestDto;
import com.easyterview.wingterview.interview.dto.response.InterviewStatusDto;
import com.easyterview.wingterview.interview.dto.response.NextRoundDto;
import com.easyterview.wingterview.interview.dto.response.QuestionCreationResponseDto;

public interface InterviewService {
    NextRoundDto goNextStage(String interviewId);

    InterviewStatusDto getInterviewStatus();

    QuestionCreationResponseDto makeQuestion(String interviewId, QuestionCreationRequestDto dto);

    void selectQuestion(String interviewId, QuestionSelectionRequestDto dto);

    void sendFeedback(String interviewId, FeedbackRequestDto dto);
}
