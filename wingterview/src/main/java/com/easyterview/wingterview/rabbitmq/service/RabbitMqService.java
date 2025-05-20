package com.easyterview.wingterview.rabbitmq.service;

import com.easyterview.wingterview.interview.dto.request.FollowUpQuestionRequest;
import com.easyterview.wingterview.interview.dto.response.QuestionCreationResponseDto;

public interface RabbitMqService {


    /**
     * 1. Queue 로 메세지를 발행
     * 2. Producer 역할 -> Topic Exchange 전략
     **/
    public QuestionCreationResponseDto sendFollowUpBlocking(FollowUpQuestionRequest requestDto);
    public QuestionCreationResponseDto receiveFollowupRequest(FollowUpQuestionRequest requestDto);
}
