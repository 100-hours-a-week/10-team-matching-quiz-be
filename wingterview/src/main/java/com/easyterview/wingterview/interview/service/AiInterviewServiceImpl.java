package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.AlreadyEnqueuedException;
import com.easyterview.wingterview.global.exception.InvalidTokenException;
import com.easyterview.wingterview.interview.dto.request.TimeInitializeRequestDto;
import com.easyterview.wingterview.interview.dto.response.AiInterviewResponseDto;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.interview.entity.InterviewParticipantEntity;
import com.easyterview.wingterview.interview.entity.InterviewTimeEntity;
import com.easyterview.wingterview.interview.enums.ParticipantRole;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.rabbitmq.consumer.FeedbackConsumer;
import com.easyterview.wingterview.rabbitmq.service.RabbitMqService;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.RecordRepository;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiInterviewServiceImpl implements AiInterviewService{

    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final UserRepository userRepository;
    private final InterviewTimeRepository interviewTimeRepository;
    private final InterviewHistoryRepository interviewHistoryRepository;


    @Transactional
    @Override
    public AiInterviewResponseDto startAiInterview(TimeInitializeRequestDto requestDto) {
        // 1. 사용자 인증
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);

        // 2. 대기열 중복 여부 확인
        if (interviewParticipantRepository.findByUser(user).isPresent()) {
            throw new AlreadyEnqueuedException();
        }

        // 3. 인터뷰 엔티티 생성
        InterviewEntity interview = InterviewEntity.builder()
                .isAiInterview(true)
                .build();

        InterviewHistoryEntity interviewHistory = InterviewHistoryEntity.builder()
                .user(user)
                .build();

        // 4. 인터뷰 엔티티 먼저 저장 (ID 생성)
        interviewRepository.save(interview);
        user.getInterviewHistoryEntityList().add(interviewHistory);
        interviewHistoryRepository.save(interviewHistory);

        // 5. 참여자 엔티티 생성 및 연결
        InterviewParticipantEntity interviewee = InterviewParticipantEntity.builder()
                .user(user)
                .role(ParticipantRole.SECOND_INTERVIEWER)
                .interview(interview) // 이미 저장된 interview 연결
                .build();

        // 6. 시간 설정
        InterviewTimeEntity interviewTime = InterviewTimeEntity.builder()
                .endAt(Timestamp.valueOf(LocalDateTime.now().plusMinutes(requestDto.getTime())))
                .interview(interview) // 이미 저장된 interview 연결
                .build();

        // 7. 양방향 관계 설정
        interview.setParticipants(List.of(interviewee));
        interview.setInterviewTime(interviewTime);

        // 8. 나머지 엔티티 저장
        interviewParticipantRepository.save(interviewee);
        interviewTimeRepository.save(interviewTime);

        // 9. 응답 반환x
        return AiInterviewResponseDto.builder()
                .interviewId(String.valueOf(interview.getId()))
                .build();
    }

}
