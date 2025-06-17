package com.easyterview.wingterview.rabbitmq.service;

import com.easyterview.wingterview.interview.dto.request.FollowUpQuestionRequest;
import com.easyterview.wingterview.interview.dto.request.STTFeedbackRequestDto;
import com.easyterview.wingterview.interview.dto.response.FollowUpQuestionResponseDto;
import com.easyterview.wingterview.quiz.dto.request.QuizCreationRequestDto;
import com.easyterview.wingterview.rabbitmq.dto.request.ChatMessage;
import com.easyterview.wingterview.rabbitmq.dto.request.ChatRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Service
@Slf4j
public class RabbitMqServiceImpl implements RabbitMqService {

    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;
    private final RestClient restClient;

    private static final long MAX_ALLOWED_TIME_MS = 10_000L;
    private static final long PER_TASK_TIME_MS = 3_000L;

    private final AtomicInteger localQueueTracker = new AtomicInteger(0);

    private final Object lock = new Object();

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Value("${ai.follow-up-url}")
    private String followUpUrl;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.api-url}")
    private String apiUrl;

    @Override
    public FollowUpQuestionResponseDto sendFollowUpBlocking(FollowUpQuestionRequest requestDto) {
        log.info("➡️ 꼬리질문 요청 메시지 전송 시작: {}", requestDto);

        long estimatedResponseTime = 0L;
        int queueSize = 0;
        synchronized (lock) {
            // 1. 현재 큐 크기 조회
            queueSize = localQueueTracker.incrementAndGet();

            // 2. 예상 응답 시간 계산
            estimatedResponseTime = queueSize * PER_TASK_TIME_MS;

            log.info("⏱️ 예상 처리 시간: {}ms (, queueSize={})",
                    estimatedResponseTime, queueSize);
        }

        // 3. 예상 응답 시간 초과 시 api 요청
        if (queueSize > 0 && estimatedResponseTime > MAX_ALLOWED_TIME_MS) {
            localQueueTracker.decrementAndGet();
            ObjectMapper mapper = new ObjectMapper();

            String prompt = buildPrompt(
                    requestDto.getSelectedQuestion(),
                    requestDto.getKeyword(),
                    requestDto.getPassedQuestions()
            );

            ChatMessage message = ChatMessage.builder()
                    .content(prompt)
                    .role("user")
                    .build();

            ChatRequest request = ChatRequest.builder()
                    .model("gpt-4o-mini")  // ✅ 반드시 유효한 모델명으로 수정
                    .messages(List.of(message))
                    .temperature(0.8)
                    .build();

            // JSON 문자열로 변환
            String requestBody;
            try {
                requestBody = mapper.writeValueAsString(request);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON 직렬화 오류", e);
            }

            // 실제 전송
            ResponseEntity<String> response = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .toEntity(String.class);

            // 응답 파싱
            try {
                JsonNode jsonNode = mapper.readTree(response.getBody());
                String content = jsonNode.get("choices").get(0).get("message").get("content").asText();
                List<String> questions = mapper.readValue(content, List.class);
                return new FollowUpQuestionResponseDto("무슨 메시지??",requestDto.getInterviewId(), questions); // 실제 리턴은 적절히 수정
            } catch (JsonProcessingException e) {
                throw new RuntimeException("응답 JSON 파싱 실패", e);
            }
        }



        // 4. 실제 요청
        Object response = rabbitTemplate.convertSendAndReceive(exchange, routingKey, requestDto);

        // 5. 응답 검증
        if (response == null) {
            throw new RuntimeException("❌ 꼬리질문 응답이 null입니다.");
        }

        // 6. 응답 처리 시간 업데이트

        // TODO : 마지막 dequeue time을 활용하여 정밀하게 계산은 아직 못했습니다.
//        lastDequeueTime.set(System.currentTimeMillis());
        localQueueTracker.decrementAndGet();

        FollowUpQuestionResponseDto dto = (FollowUpQuestionResponseDto) response;
        log.info("✅ 꼬리질문 응답 수신 완료: {}", dto);
        return dto;
    }

    private String buildPrompt(String selectedQuestion, String keyword,
                               List<String> passedQuestions) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 IT 직무 면접 질문 생성 AI입니다. 주어진 정보를 바탕으로, 메인 질문에 대한 심층적인 꼬리 질문 4개를 한국어로 생성해 주세요.\n\n");
        sb.append("[메인 질문]\n").append(selectedQuestion).append("\n\n");

        if (keyword != null && !keyword.isBlank()) {
            sb.append("[사용자 키워드]\n").append(keyword).append("\n\n");
        }

        if (passedQuestions != null && !passedQuestions.isEmpty()) {
            sb.append("[이전 질문 목록]\n").append(String.join("\n",
                    passedQuestions)).append("\n\n");
        }

        sb.append("[조건]\n");
        sb.append("1. 사용자 키워드가 있다면, 각 질문에 해당 키워드를 **자연스럽게 무조건 반영**하세요.\n");
        sb.append("2. 생성되는 질문은 [메인 질문]과 **내용이 겹치면 안 됩니다.**\n");
        sb.append("3. [이전 질문 목록]이 있다면, 해당 질문들과도 **내용이 중복되지 않도록** 하세요.\n");
        sb.append("4. 각 질문은 서로 **다른 관점**을 반영해야 합니다.\n");
        sb.append("5. 모든 질문은 **완전한 문장 형태**여야 하며, `\"...\"`,생략 부호, 명확하지 않은 표현은 절대 사용하지 마세요.\n");
        sb.append("6. *단답형 질문**이나 **너무 광범위한 질문**은 피해주세요.\n");
        sb.append("7. 각 질문은 **한글 기준 100자 이내**로 작성해 주세요.\n");
        sb.append("8. 결과는 반드시 아래 JSON 형식으로 출력해 주세요:\n\n");
        sb.append("[\n \"질문 1\",\n \"질문 2\",\n \"질문 3\",\n \"질문4\"\n]");

        return sb.toString();
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

//    @Override
//    public void sendFeedbackRequest(AiFeedbackRequestDto dto) {
//        rabbitTemplate.convertAndSend("feedback.request.exchange", "feedback.request.routingKey", dto);
//    }

    @Override
    public void sendQuizCreation(QuizCreationRequestDto request) {
        rabbitTemplate.convertAndSend("quiz.request.exchange", "quiz.request.routingKey", request);
        log.info("📤 복습 퀴즈 생성 요청 전송: {}", request);
    }

    @Override
    public void sendSTTFeedbackRequest(STTFeedbackRequestDto request) {
        rabbitTemplate.convertAndSend("feedback.request.exchange","feedback.request.routingKey",request);
        log.info("📤 STT 피드백 생성 요청 전송: {}", request);
    }

//    @RabbitListener(queues = "feedback.response.queue")
//    public void handleFeedback(AiFeedbackResponseDto response) {
//        log.info("📩 피드백 응답 수신: {}", response);
//
//        // 1. 최신 InterviewHistory 찾기
//        InterviewHistoryEntity interviewHistory = interviewHistoryRepository
//                .findFirstByUserIdOrderByCreatedAtDesc(response.getUserId())
//                .orElseThrow(() -> new RuntimeException("InterviewHistory not found"));
//
//        // 2. FeedbackEntity 저장 (이건 너가 만들 구조에 맞게!)
//        InterviewFeedbackEntity feedback = InterviewFeedbackEntity.builder()
//                .interviewHistory(interviewHistory)
//                .feedback(response.getFeedback())
//                .score(response.getScore())
//                .build();
//
//        feedbackRepository.save(feedback);
//        log.info("✅ 피드백 저장 완료");
//    }
}


