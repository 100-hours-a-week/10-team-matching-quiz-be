package com.easyterview.wingterview.matching.service;

import com.easyterview.wingterview.common.util.SeatPositionUtil;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.AlreadyEnqueuedException;
import com.easyterview.wingterview.global.exception.InvalidTokenException;
import com.easyterview.wingterview.global.exception.MatchingClosedException;
import com.easyterview.wingterview.global.exception.UserNotParticipatedException;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewParticipantEntity;
import com.easyterview.wingterview.interview.enums.ParticipantRole;
import com.easyterview.wingterview.interview.repository.InterviewParticipantRepository;
import com.easyterview.wingterview.interview.repository.InterviewRepository;
import com.easyterview.wingterview.matching.config.MatchingStatusManager;
import com.easyterview.wingterview.matching.dto.response.Interviewee;
import com.easyterview.wingterview.matching.dto.response.Interviewer;
import com.easyterview.wingterview.matching.dto.response.MatchingResultDto;
import com.easyterview.wingterview.matching.dto.response.MatchingStatisticsDto;
import com.easyterview.wingterview.matching.entity.MatchingEntity;
import com.easyterview.wingterview.matching.repository.MatchingRepository;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final MatchingRepository matchingRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final MatchingStatusManager matchingStatusManager;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void enqueue() {
        if (!matchingStatusManager.isMatchingOpen()) {
            throw new MatchingClosedException();
        }

        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);

        Optional<MatchingEntity> matching = matchingRepository.findByUser(user);
        if(matching.isEmpty()){
            matchingRepository.save(MatchingEntity.builder()
                    .user(user)
                    .build());
        }
        else{
            throw new AlreadyEnqueuedException();
        }

    }

    @Transactional(readOnly = true)
    @Override
    public MatchingResultDto getMatchingResult() {
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);

        // TODO : 인터뷰 참가자가 아닌데 APi 호출하면 UserNotParticipatedException 호출해야하는데
        // 이걸 matchingEntity들을 매칭이 됐다고 지우지 말고 면접이 끝날 때 지우면 되겠네.
        MatchingEntity matching = matchingRepository.findByUser(user)
                .orElseThrow(UserNotParticipatedException::new);

        // 인터뷰가 만들어졌다면 인터뷰 참가자 엔터티가 존재해야함
        Optional<InterviewParticipantEntity> interviewParticipantOpt = interviewParticipantRepository.findByUser(user);
        if(interviewParticipantOpt.isEmpty())
            return null;

        InterviewEntity interview = interviewParticipantOpt.get().getInterview();

        Map<ParticipantRole, UserEntity> participants = interview.getParticipants().stream()
                .collect(Collectors.toMap(InterviewParticipantEntity::getRole, InterviewParticipantEntity::getUser));

        ParticipantRole myRole = interview.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .map(InterviewParticipantEntity::getRole)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("인터뷰 참여자 목록에 사용자가 없습니다."));

        boolean isFirst = (myRole == ParticipantRole.FIRST_INTERVIEWER);

        UserEntity interviewerUser = participants.get(ParticipantRole.FIRST_INTERVIEWER);
        UserEntity intervieweeUser = participants.get(ParticipantRole.SECOND_INTERVIEWER);

        Interviewer interviewer = Interviewer.builder()
                .name(interviewerUser.getName())
                .nickname(interviewerUser.getNickname())
                .curriculum(interviewerUser.getCurriculum())
                .jobInterest(interviewerUser.getUserJobInterest().stream()
                        .map(i -> i.getJobInterest().getLabel()).toList())
                .techStack(interviewerUser.getUserTechStack().stream()
                        .map(i -> i.getTechStack().getLabel()).toList())
                .seatCode(SeatPositionUtil.seatIdxToSeatCode(interviewerUser.getSeat()))
                .build();

        Interviewee interviewee = Interviewee.builder()
                .name(intervieweeUser.getName())
                .nickname(intervieweeUser.getNickname())
                .curriculum(intervieweeUser.getCurriculum())
                .jobInterest(intervieweeUser.getUserJobInterest().stream()
                        .map(i -> i.getJobInterest().getLabel()).toList())
                .techStack(intervieweeUser.getUserTechStack().stream()
                        .map(i -> i.getTechStack().getLabel()).toList())
                .build();

        return MatchingResultDto.builder()
                .isFirstInterviewer(isFirst)
                .isAiInterview(interview.getIsAiInterview())
                .interviewer(interviewer)
                .interviewee(interviewee)
                .interviewId(interview.getId().toString())
                .build();
    }

    @Override
    public MatchingStatisticsDto getMatchingStatistics() {
        if(!matchingStatusManager.isMatchingOpen())
            throw new MatchingClosedException();

        return MatchingStatisticsDto.builder()
                .count((int)matchingRepository.count())
                .build();
    }

}
