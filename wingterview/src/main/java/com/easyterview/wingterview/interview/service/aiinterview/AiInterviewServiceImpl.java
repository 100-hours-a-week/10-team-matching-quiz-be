package com.easyterview.wingterview.interview.service.aiinterview;

import com.easyterview.wingterview.common.util.mapper.entity.AiInterviewParticipantMapper;
import com.easyterview.wingterview.common.util.mapper.entity.InterviewHistoryMapper;
import com.easyterview.wingterview.common.util.mapper.entity.InterviewMapper;
import com.easyterview.wingterview.common.util.mapper.entity.InterviewTimeMapper;
import com.easyterview.wingterview.global.exception.AlreadyEnqueuedException;
import com.easyterview.wingterview.interview.dto.request.TimeInitializeRequestDto;
import com.easyterview.wingterview.interview.dto.response.AiInterviewResponseDto;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.interview.entity.InterviewParticipantEntity;
import com.easyterview.wingterview.interview.entity.InterviewTimeEntity;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.provider.UserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiInterviewServiceImpl implements AiInterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final UserProvider userProvider;
    private final InterviewTimeRepository interviewTimeRepository;
    private final InterviewHistoryRepository interviewHistoryRepository;

    @Transactional
    @Override
    public AiInterviewResponseDto startAiInterview(TimeInitializeRequestDto requestDto) {
        // 유저 정보 및 예외처리
        UserEntity user = userProvider.getUserOrThrow();
        checkNotAlreadyEnqueued(user);

        // 인터뷰 엔티티 만들기
        InterviewEntity interview = InterviewMapper.toEntity(true);
        interviewRepository.save(interview);

        // 인터뷰 히스토리 엔티티 만들기
        InterviewHistoryEntity interviewHistory = InterviewHistoryMapper.toEntity(user);
        user.getInterviewHistoryEntityList().add(interviewHistory); // 양방향 관계 설정
        interviewHistoryRepository.save(interviewHistory);

        // 인터뷰 참가자 엔티티 만들기
        InterviewParticipantEntity participant = AiInterviewParticipantMapper.toEntity(user, interview);
        InterviewTimeEntity interviewTime = InterviewTimeMapper.toEntity(requestDto.getTime(), interview);

        // 연관관계 설정 및 저장
        relateEntities(interview, participant, interviewTime);
        saveEntities(participant, interviewTime);

        return AiInterviewResponseDto.toDto(interview.getId().toString());
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

    private void checkNotAlreadyEnqueued(UserEntity user) {
        if (interviewParticipantRepository.findByUser(user).isPresent()) {
            throw new AlreadyEnqueuedException();
        }
    }
}

