package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.interview.dto.request.*;
import com.easyterview.wingterview.interview.dto.response.*;
import com.easyterview.wingterview.interview.service.aiinterview.AiInterviewService;
import com.easyterview.wingterview.interview.service.feedback.FeedbackService;
import com.easyterview.wingterview.interview.service.interviewflow.InterviewFlowService;
import com.easyterview.wingterview.interview.service.question.QuestionGenerationFacade;
import com.easyterview.wingterview.interview.service.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewFacade {
    private final InterviewFlowService interviewFlowService;
    private final FeedbackService feedbackService;
    private final AiInterviewService aiInterviewService;
    private final QuestionGenerationFacade questionGenerationFacade;

    public NextRoundDto goNextStage(String interviewId){
        return interviewFlowService.goNextStage(interviewId);
    }

    public Object getInterviewStatus(){
        return interviewFlowService.getInterviewStatus();
    }

    public Object makeQuestion(String interviewId, QuestionCreationRequestDto dto){
        return questionGenerationFacade.makeQuestion(interviewId, dto);
    }

    public void selectQuestion(String interviewId, QuestionSelectionRequestDto dto) {
        questionGenerationFacade.selectQuestion(interviewId, dto);
    }

    public void sendFeedback(String interviewId, FeedbackRequestDto dto) {
        feedbackService.sendFeedback(interviewId,dto);
    }

    public AiInterviewResponseDto startAiInterview(TimeInitializeRequestDto requestDto) {
        return aiInterviewService.startAiInterview(requestDto);
    }

    public void exitInterview(String interviewId) {
        interviewFlowService.exitInterview(interviewId);
    }

    public InterviewIdResponse getInterviewId(String userId) {
        return interviewFlowService.getInterviewId(userId);
    }

    public void requestSttFeedback(String userId) {
        feedbackService.requestSttFeedback(userId);
    }
}

