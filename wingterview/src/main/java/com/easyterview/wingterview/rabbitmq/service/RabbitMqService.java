package com.easyterview.wingterview.rabbitmq.service;

import com.easyterview.wingterview.interview.dto.request.FollowUpQuestionRequest;
import com.easyterview.wingterview.interview.dto.response.FollowUpQuestionResponseDto;
import com.easyterview.wingterview.quiz.dto.request.QuizCreationRequestDto;

public interface RabbitMqService {


    /**
     * 1. Queue 로 메세지를 발행
     * 2. Producer 역할 -> Topic Exchange 전략
     **/
    FollowUpQuestionResponseDto sendFollowUpBlocking(FollowUpQuestionRequest requestDto);
    FollowUpQuestionResponseDto receiveFollowupRequest(FollowUpQuestionRequest requestDto);
//    void sendFeedbackRequest(AiFeedbackRequestDto dto);

    void sendQuizCreation(QuizCreationRequestDto request);
}
