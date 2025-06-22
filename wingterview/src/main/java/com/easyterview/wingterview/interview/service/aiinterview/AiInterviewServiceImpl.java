package com.easyterview.wingterview.interview.service.aiinterview;

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
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiInterviewServiceImpl implements AiInterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final UserRepository userRepository;
    private final InterviewTimeRepository interviewTimeRepository;
    private final InterviewHistoryRepository interviewHistoryRepository;

    @Transactional
    @Override
    public AiInterviewResponseDto startAiInterview(TimeInitializeRequestDto requestDto) {
        // 유저 정보 및 예외처리
        UserEntity user = getUserOrThrow();
        checkNotAlreadyEnqueued(user);

        // 인터뷰, 인터뷰 참가자, 인터뷰 시간 엔티티 만들기
        InterviewEntity interview = createInterview();
        createInterviewHistory(user);
        InterviewParticipantEntity participant = createParticipant(user, interview);
        InterviewTimeEntity interviewTime = createInterviewTime(interview, requestDto.getTime());

        // 연관관계 설정 및 저장
        relateEntities(interview, participant, interviewTime);
        saveEntities(participant, interviewTime);

        return AiInterviewResponseDto.builder()
                .interviewId(interview.getId().toString())
                .build();
    }

    // ======================== 👇 헬퍼 메서드들 👇 ========================

    private void relateEntities(InterviewEntity interview, InterviewParticipantEntity participant, InterviewTimeEntity interviewTime) {
        interview.setParticipants(List.of(participant));
        interview.setInterviewTime(interviewTime);
    }

    private void saveEntities(InterviewParticipantEntity participant, InterviewTimeEntity interviewTime) {
        interviewParticipantRepository.save(participant);
        interviewTimeRepository.save(interviewTime);
    }

    private UserEntity getUserOrThrow() {
        return userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);
    }

    private void checkNotAlreadyEnqueued(UserEntity user) {
        if (interviewParticipantRepository.findByUser(user).isPresent()) {
            throw new AlreadyEnqueuedException();
        }
    }

    private InterviewEntity createInterview() {
        InterviewEntity interview = InterviewEntity.builder()
                .isAiInterview(true)
                .build();
        return interviewRepository.save(interview);
    }

    private void createInterviewHistory(UserEntity user) {
        InterviewHistoryEntity history = InterviewHistoryEntity.builder()
                .user(user)
                .build();
        user.getInterviewHistoryEntityList().add(history); // 양방향 관계 설정
        interviewHistoryRepository.save(history);
    }

    private InterviewParticipantEntity createParticipant(UserEntity user, InterviewEntity interview) {
        return InterviewParticipantEntity.builder()
                .user(user)
                .role(ParticipantRole.SECOND_INTERVIEWER)
                .interview(interview)
                .build();
    }

    private InterviewTimeEntity createInterviewTime(InterviewEntity interview, int timeMinutes) {
        return InterviewTimeEntity.builder()
                .endAt(Timestamp.valueOf(LocalDateTime.now().plusMinutes(timeMinutes)))
                .interview(interview)
                .build();
    }
}

