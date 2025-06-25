package com.easyterview.wingterview.interview.service.feedback;

import com.easyterview.wingterview.common.util.mapper.dto.STTFeedbackRequestMapper;
import com.easyterview.wingterview.common.util.mapper.dto.QuestionSegmentMapper;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.interview.dto.request.FeedbackRequestDto;
import com.easyterview.wingterview.interview.dto.request.QuestionSegment;
import com.easyterview.wingterview.interview.dto.response.FeedbackResponseDto;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.interview.provider.InterviewProvider;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.rabbitmq.consumer.FeedbackConsumer;
import com.easyterview.wingterview.rabbitmq.service.RabbitMqService;
import com.easyterview.wingterview.user.entity.RecordingEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.provider.UserProvider;
import com.easyterview.wingterview.user.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService{

    private final InterviewProvider interviewProvider;
    private final UserProvider userProvider;
    private final InterviewHistoryRepository interviewHistoryRepository;
    private final RecordRepository recordRepository;
    private final FeedbackConsumer feedbackConsumer;
    private final RabbitMqService rabbitMqService;

    // AI 피드백 request API 필요함
    // feedback requested를 true로 바꿔준다.
    @Override
    @Transactional
    public void requestSttFeedback(String userId) {
        InterviewHistoryEntity interviewHistory = getLastInterviewHistoryOrElseThrow(userId);
        RecordingEntity recordingEntity = getLastRecordingOrElseThrow(interviewHistory);
        List<QuestionSegment> questionSegments = QuestionSegmentMapper.of(interviewHistory);

        rabbitMqService.sendSTTFeedbackRequest(STTFeedbackRequestMapper.of(questionSegments, recordingEntity));
    }

    // 상대방에게 피드백 보내는 로직 -> 현재는 사용 x -> 추후 1:1 매칭 및 면접 기능 활성화시 사용
    @Transactional
    @Override
    public void sendFeedback(String interviewId, FeedbackRequestDto dto) {
        // 1. 인터뷰 정보 조회
        InterviewEntity interview = interviewProvider.getInterviewOrThrow(interviewId);

        // 2. 현재 로그인된 사용자 조회
        UserEntity currentUser = userProvider.getUserOrThrow();

        // 3. 상대방 유저 찾기 (현재 유저와 ID가 다른 참여자)
        UserEntity otherUser = userProvider.findPartner(interview, currentUser);
    }

    @RabbitListener(queues = "feedback.response.queue")
    public void handleFeedbackResponse(FeedbackResponseDto responseDto) {
        feedbackConsumer.consumeFeedback(responseDto);
    }

    // ======================== 👇 헬퍼 메서드들 👇 ========================

    private RecordingEntity getLastRecordingOrElseThrow(InterviewHistoryEntity interviewHistory) {
        return recordRepository.findByInterviewHistoryId(interviewHistory.getId()).orElseThrow(InterviewNotFoundException::new);
    }

    private InterviewHistoryEntity getLastInterviewHistoryOrElseThrow(String userId) {
        return interviewHistoryRepository.findFirstByUserIdOrderByCreatedAtDesc(UUID.fromString(userId)).orElseThrow(InterviewNotFoundException::new);
    }
}
