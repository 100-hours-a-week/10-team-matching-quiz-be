package com.easyterview.wingterview.rabbitmq.service;

import com.easyterview.wingterview.interview.dto.request.FollowUpQuestionRequest;
import com.easyterview.wingterview.interview.dto.response.QuestionCreationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
@Slf4j
public class RabbitMqServiceImpl implements RabbitMqService {

    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Value("${ai.follow-up-url}")
    private String followUpUrl;

    @Override
    public QuestionCreationResponseDto sendFollowUpBlocking(FollowUpQuestionRequest requestDto) {
        log.info("➡️ 꼬리질문 요청 메시지 전송 시작: {}", requestDto);
        Object response = rabbitTemplate.convertSendAndReceive(exchange, routingKey, requestDto);

        if (response == null) {
            throw new RuntimeException("❌ 꼬리질문 응답이 null입니다.");
        }

        QuestionCreationResponseDto dto = (QuestionCreationResponseDto) response;
        log.info("✅ 꼬리질문 응답 수신 완료: {}", dto);
        return dto;
    }

    @Override
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public QuestionCreationResponseDto receiveFollowupRequest(FollowUpQuestionRequest requestDto) {
        log.info("📩 꼬리질문 요청 수신: {}", requestDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<FollowUpQuestionRequest> entity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<QuestionCreationResponseDto> response = restTemplate.postForEntity(
                followUpUrl,
                entity,
                QuestionCreationResponseDto.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("❌ 꼬리질문 생성 서버 응답 실패");
        }

        log.info("📤 꼬리질문 응답 전송: {}", response.getBody());
        return response.getBody();
    }
}
