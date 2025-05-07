package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.common.util.InterviewStatus;
import com.easyterview.wingterview.common.util.InterviewUtil;
import com.easyterview.wingterview.common.util.TimeUtil;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.global.exception.InvalidTokenException;
import com.easyterview.wingterview.global.exception.InvalidUUIDFormatException;
import com.easyterview.wingterview.global.exception.UserNotParticipatedException;
import com.easyterview.wingterview.interview.dto.response.InterviewStatusDto;
import com.easyterview.wingterview.interview.dto.response.NextRoundDto;
import com.easyterview.wingterview.interview.dto.response.Partner;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewParticipantEntity;
import com.easyterview.wingterview.interview.enums.ParticipantRole;
import com.easyterview.wingterview.interview.repository.InterviewParticipantRepository;
import com.easyterview.wingterview.interview.repository.InterviewRepository;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NextRoundDto goNextStage(String interviewId) {
        try {
            // 인터뷰Id로 인터뷰 조회
            Optional<InterviewEntity> interviewOpt = interviewRepository.findById(UUID.fromString(interviewId));

            // 없으면 예외 던지기
            if (interviewOpt.isEmpty()) {
                throw new InterviewNotFoundException();
            }

            // 인터뷰 다음 분기로 바꾸기
            InterviewEntity interview = interviewOpt.get();
            InterviewStatus nextStatus = InterviewUtil.nextPhase(interview.getRound(), interview.getPhase(), interview.getIsAiInterview());
            interview.setPhase(nextStatus.getPhase());
            interview.setRound(nextStatus.getRound());

            // 바꾼 분기 dto 리턴
            return NextRoundDto.builder()
                    .currentPhase(nextStatus.getPhase().getPhase())
                    .currentRound(nextStatus.getRound())
                    .build();
        }

        // uuid 이상하면 예외 던지기
        catch (IllegalArgumentException e) {
            throw new InvalidUUIDFormatException();
        }
    }

    @Override
    @Transactional
    public InterviewStatusDto getInterviewStatus() {

        // 유저 정보 -> 인터뷰 정보 가져오기
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);
        InterviewParticipantEntity interviewParticipant = interviewParticipantRepository.findByUser(user)
                .orElseThrow(UserNotParticipatedException::new);
        InterviewEntity interview = interviewParticipant.getInterview();

        InterviewParticipantEntity partnerParticipant = interview.getParticipants()
                .stream()
                .filter(i -> !i.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("상대 유저를 찾을 수 없습니다."));

        UserEntity partnerEntity = partnerParticipant.getUser();

        int timeRemain = TimeUtil.getRemainTime(interview.getPhaseAt(), InterviewStatus.builder().round(interview.getRound()).phase(interview.getPhase()).build());
        boolean isInterviewer = InterviewUtil.checkInterviewer(interviewParticipant.getRole(), interview.getRound());
        Partner partner = Partner.builder()
                .name(partnerEntity.getName())
                .nickname(partnerEntity.getNickname())
                .profileImageUrl(partnerEntity.getProfileImageUrl())
                .techStack(partnerEntity.getUserTechStack().stream().map(t -> t.getTechStack().getLabel()).toList())
                .jobInterest(partnerEntity.getUserJobInterest().stream().map(j -> j.getJobInterest().getLabel()).toList())
                .curriculum(partnerEntity.getCurriculum())
                .build();

        return InterviewStatusDto.builder()
                .interviewId(String.valueOf(interview.getId()))
                .timeRemain(timeRemain)
                // TODO : 시간에 맞게 round, phase 계산해주기 ??
                .currentRound(interview.getRound())
                .currentPhase(interview.getPhase().getPhase())
                .isInterviewer(isInterviewer)
                .isAiInterview(interview.getIsAiInterview())

                .partner(partner)
                // TODO : 질문
//                .questionIdx()
//                .selectedQuestion()
//                .questionOption()
                .build();


//        InterviewStatusDto
    }
}
