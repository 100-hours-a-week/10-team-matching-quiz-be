package com.easyterview.wingterview.rabbitmq.service;

import com.easyterview.wingterview.interview.dto.request.FollowUpQuestionRequest;
import com.easyterview.wingterview.interview.dto.response.FollowUpQuestionResponseDto;
import com.easyterview.wingterview.interview.dto.response.QuestionCreationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@Service
@Slf4j
public class RabbitMqServiceImpl implements RabbitMqService {

    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;
    private final AmqpAdmin amqpAdmin;

    private static final long MAX_ALLOWED_TIME_MS = 10_000L;
    private static final long PER_TASK_TIME_MS = 4_000L;

    private static final AtomicLong lastDequeueTime = new AtomicLong(System.currentTimeMillis());


    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Value("${ai.follow-up-url}")
    private String followUpUrl;

    @Override
    public FollowUpQuestionResponseDto sendFollowUpBlocking(FollowUpQuestionRequest requestDto) {
        log.info("➡️ 꼬리질문 요청 메시지 전송 시작: {}", requestDto);

        // 1. 현재 큐 크기 조회
        Properties queueProps = amqpAdmin.getQueueProperties("ai.request.queue");
        int queueSize = (queueProps != null && queueProps.get("QUEUE_MESSAGE_COUNT") != null)
                ? (Integer) queueProps.get("QUEUE_MESSAGE_COUNT")
                : 0;

        System.out.println("***********");
        System.out.println(System.currentTimeMillis());
        System.out.println(lastDequeueTime.get());

        // 2. 예상 응답 시간 계산
        long now = System.currentTimeMillis();
        long timeSinceLastResponse = now - lastDequeueTime.get();
        long estimatedResponseTime = timeSinceLastResponse + (queueSize + 1) * PER_TASK_TIME_MS;

        log.info("⏱️ 예상 처리 시간: {}ms (lastDequeue={}ms ago, queueSize={})",
                estimatedResponseTime, timeSinceLastResponse, queueSize);

        // 3. 제한 초과 시 예외 발생 또는 null 반환
        if (queueSize > 0 && estimatedResponseTime > MAX_ALLOWED_TIME_MS) {
            // TODO : api 요청으로 응답을 받아오기
//            receiveFollowupRequestFromAPI();
            throw new RuntimeException("❌ 예상 처리 시간 초과로 요청을 전송하지 않았습니다.");
        }

        // 4. 실제 요청
        Object response = rabbitTemplate.convertSendAndReceive(exchange, routingKey, requestDto);

        // 5. 응답 검증
        if (response == null) {
            throw new RuntimeException("❌ 꼬리질문 응답이 null입니다.");
        }

        // 6. 응답 처리 시간 업데이트
        lastDequeueTime.set(System.currentTimeMillis());

        FollowUpQuestionResponseDto dto = (FollowUpQuestionResponseDto) response;
        log.info("✅ 꼬리질문 응답 수신 완료: {}", dto);
        return dto;
    }

    @Override
    @RabbitListener(
            queues = "ai.request.queue",
            containerFactory = "rabbitListenerContainerFactory"  // 👈 이거 명시!
    )
    public FollowUpQuestionResponseDto receiveFollowupRequest(FollowUpQuestionRequest requestDto) {
        log.info("📩 꼬리질문 요청 수신: {}", requestDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<FollowUpQuestionRequest> entity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<FollowUpQuestionResponseDto> response = restTemplate.postForEntity(
                followUpUrl,
                entity,
                FollowUpQuestionResponseDto.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("❌ 꼬리질문 생성 서버 응답 실패");
        }

        log.info("📤 꼬리질문 응답 전송: {}", response.getBody());
        return response.getBody();
    }
}
