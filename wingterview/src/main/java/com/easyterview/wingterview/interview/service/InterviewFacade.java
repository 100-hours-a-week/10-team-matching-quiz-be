package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.common.util.InterviewStatus;
import com.easyterview.wingterview.common.util.InterviewUtil;
import com.easyterview.wingterview.common.util.TimeUtil;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.*;
import com.easyterview.wingterview.interview.dto.request.*;
import com.easyterview.wingterview.interview.dto.response.*;
import com.easyterview.wingterview.interview.entity.*;
import com.easyterview.wingterview.interview.enums.ParticipantRole;
import com.easyterview.wingterview.interview.enums.Phase;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.rabbitmq.consumer.FeedbackConsumer;
import com.easyterview.wingterview.rabbitmq.service.RabbitMqService;
import com.easyterview.wingterview.user.entity.RecordingEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.RecordRepository;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class InterviewFacade {
    private final InterviewFlowService interviewFlowService;
    private final FeedbackService feedbackService;
    private final AiInterviewService aiInterviewService;
    private final QuestionService questionService;

    public NextRoundDto goNextStage(String interviewId){
        return interviewFlowService.goNextStage(interviewId);
    }

    public Object getInterviewStatus(){
        return interviewFlowService.getInterviewStatus();
    }

    public Object makeQuestion(String interviewId, QuestionCreationRequestDto dto){
        return questionService.makeQuestion(interviewId, dto);
    }

    public void selectQuestion(String interviewId, QuestionSelectionRequestDto dto) {
        questionService.selectQuestion(interviewId, dto);
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

