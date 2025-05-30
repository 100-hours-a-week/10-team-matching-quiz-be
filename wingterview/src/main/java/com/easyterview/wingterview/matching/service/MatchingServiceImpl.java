package com.easyterview.wingterview.matching.service;

import com.easyterview.wingterview.common.util.SeatPositionUtil;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.*;
import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewParticipantEntity;
import com.easyterview.wingterview.interview.entity.InterviewTimeEntity;
import com.easyterview.wingterview.interview.enums.ParticipantRole;
import com.easyterview.wingterview.interview.repository.InterviewParticipantRepository;
import com.easyterview.wingterview.interview.repository.InterviewRepository;
import com.easyterview.wingterview.matching.algorithm.MatchingAlgorithm;
import com.easyterview.wingterview.matching.algorithm.MatchingUser;
import com.easyterview.wingterview.matching.config.MatchingStatusManager;
import com.easyterview.wingterview.matching.dto.response.Interviewee;
import com.easyterview.wingterview.matching.dto.response.Interviewer;
import com.easyterview.wingterview.matching.dto.response.MatchingResultDto;
import com.easyterview.wingterview.matching.dto.response.MatchingStatisticsDto;
import com.easyterview.wingterview.matching.entity.MatchingParticipantEntity;
import com.easyterview.wingterview.matching.repository.MatchingParticipantRepository;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final MatchingParticipantRepository matchingParticipantRepository;
    private final MatchingAlgorithm matchingAlgorithm;
    private final InterviewRepository interviewRepository;
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

        Optional<MatchingParticipantEntity> matching = matchingParticipantRepository.findByUser(user);
        if(matching.isEmpty()){
            matchingParticipantRepository.save(MatchingParticipantEntity.builder()
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
        MatchingParticipantEntity matching = matchingParticipantRepository.findByUser(user)
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
        if (!matchingStatusManager.isMatchingOpen())
            throw new MatchingClosedException();

        Integer notMatchedCount = matchingParticipantRepository.countNotMatchedParticipants();

        return MatchingStatisticsDto.builder()
                .count(notMatchedCount)
                .build();
    }

    @Transactional
    public void doMatchingAlgorithm() {
        // 매칭 큐 닫기
        matchingStatusManager.closeMatching();

        // 참여자 중 Interview에 들어있지 않은 참가자를 모두 찾아서
        List<MatchingParticipantEntity> participantList = matchingParticipantRepository.findAll();
        List<MatchingParticipantEntity> notMatchedParticipantList = participantList.stream().filter(m ->
            interviewParticipantRepository.findByUser(m.getUser()).isEmpty()
        ).toList();

        // 새로 매칭 알고리즘 돌릴 유저 List로 묶음
        List<MatchingUser> matchingUsers = notMatchedParticipantList.stream().map(participant -> {
            UserEntity user = participant.getUser();
            return
            MatchingUser.builder()
                    .userId(String.valueOf(user.getId()))
                    .jobInterests(user.getUserJobInterest().stream().map(j -> j.getJobInterest().getLabel()).toList())
                    .techStacks(user.getUserTechStack().stream().map(t -> t.getTechStack().getLabel()).toList())
                    .curriculum(user.getCurriculum())
                    .build();
        }).toList();

        // 매칭 알고리즘 돌리기
        matchingAlgorithm.setParticipants(matchingUsers);
        List<Pair<String, String>> matchingResults = matchingAlgorithm.performGreedyMatching();

        Set<String> matchedUsers = new HashSet<>();
        // result 적용
        matchingResults.forEach(p -> {
            String intervieweeId = p.getLeft();
            String interviewerId = p.getRight();

            InterviewParticipantEntity interviewer = InterviewParticipantEntity.builder()
                    .role(ParticipantRole.FIRST_INTERVIEWER)
                    .user(userRepository.findById(UUID.fromString(interviewerId)).orElseThrow(UserNotFoundException::new))
                    .build();

            InterviewParticipantEntity interviewee = InterviewParticipantEntity.builder()
                    .role(ParticipantRole.SECOND_INTERVIEWER)
                    .user(userRepository.findById(UUID.fromString(intervieweeId)).orElseThrow(UserNotFoundException::new))
                    .build();

            InterviewEntity interview = InterviewEntity.builder()
                    .participants(List.of(interviewer, interviewee))
                    .build();


            interviewee.setInterview(interview);
            interviewer.setInterview(interview);

            matchedUsers.add(intervieweeId);
            matchedUsers.add(interviewerId);

            interviewRepository.save(interview);
        });

        // 매칭 되지 않은 유저는 큐에서 제외하기(최대 1명)
        List<MatchingParticipantEntity> matchingFailedParticipants =
                notMatchedParticipantList.stream()
                        .filter(p -> !matchedUsers.contains(String.valueOf(p.getUser().getId()))).toList();

        if(!matchingFailedParticipants.isEmpty()){
            matchingParticipantRepository.deleteAllInBatch(matchingFailedParticipants);
        }

    }

    @Override
    @Transactional
    public void deleteParticipants() {
        matchingParticipantRepository.deleteAllInBatch();
    }
}
