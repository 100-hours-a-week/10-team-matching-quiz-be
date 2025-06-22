package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.global.exception.InvalidTokenException;
import com.easyterview.wingterview.interview.dto.request.FeedbackRequestDto;
import com.easyterview.wingterview.interview.dto.request.QuestionSegment;
import com.easyterview.wingterview.interview.dto.request.STTFeedbackRequestDto;
import com.easyterview.wingterview.interview.dto.response.FeedbackResponseDto;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.interview.entity.InterviewParticipantEntity;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.rabbitmq.consumer.FeedbackConsumer;
import com.easyterview.wingterview.rabbitmq.service.RabbitMqService;
import com.easyterview.wingterview.user.entity.RecordingEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.RecordRepository;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService{

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final InterviewHistoryRepository interviewHistoryRepository;
    private final RecordRepository recordRepository;
    private final FeedbackConsumer feedbackConsumer;
    private final RabbitMqService rabbitMqService;

    // AI 피드백 request API 필요함
    // feedback requested를 true로 바꿔준다.
    @Override
    @Transactional
    public void requestSttFeedback(String userId) {
        InterviewHistoryEntity interviewHistory = interviewHistoryRepository.findFirstByUserIdOrderByCreatedAtDesc(UUID.fromString(userId)).orElseThrow(InterviewNotFoundException::new);
        interviewHistory.setIsFeedbackRequested(true);
        RecordingEntity recordingEntity = recordRepository.findByInterviewHistoryId(interviewHistory.getId()).orElseThrow(InterviewNotFoundException::new);

        List<QuestionSegment> questionSegments = interviewHistory.getSegments().stream()
                .map(s -> QuestionSegment.builder()
                        .segmentId(s.getId().toString())
                        .startTime(s.getFromTime())
                        .endTime(s.getToTime())
                        .question(s.getSelectedQuestion())
                        .build()).toList();



        rabbitMqService.sendSTTFeedbackRequest(STTFeedbackRequestDto.builder()
                .questionLists(questionSegments)
                .recordingUrl(recordingEntity.getUrl())
                .build());
    }

    @Transactional
    @Override
    public void sendFeedback(String interviewId, FeedbackRequestDto dto) {
        // 1. 문자열 인터뷰 ID를 UUID로 변환
        UUID interviewUUID = UUID.fromString(interviewId);

        // 2. 인터뷰 정보 조회 (없으면 예외 발생)
        InterviewEntity interview = interviewRepository.findById(interviewUUID)
                .orElseThrow(InterviewNotFoundException::new);

        // 3. 현재 로그인된 사용자 조회
        UserEntity currentUser = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);

        // 4. 상대방 유저 찾기 (현재 유저와 ID가 다른 참여자)
        UserEntity otherUser = interview.getParticipants().stream()
                .map(InterviewParticipantEntity::getUser)
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("상대 유저를 찾을 수 없습니다."));
    }

    @RabbitListener(queues = "feedback.response.queue")
    public void handleFeedbackResponse(FeedbackResponseDto responseDto) {
        feedbackConsumer.consumeFeedback(responseDto);
    }
}
