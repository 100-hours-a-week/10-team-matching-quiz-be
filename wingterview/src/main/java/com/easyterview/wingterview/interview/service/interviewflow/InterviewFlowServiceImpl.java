package com.easyterview.wingterview.interview.service.interviewflow;

import com.easyterview.wingterview.common.util.InterviewStatus;
import com.easyterview.wingterview.common.util.InterviewUtil;
import com.easyterview.wingterview.common.util.TimeUtil;
import com.easyterview.wingterview.common.util.mapper.dto.AiInterviewInfoMapper;
import com.easyterview.wingterview.common.util.mapper.dto.InterviewStatusMapper;
import com.easyterview.wingterview.common.util.mapper.dto.NextRoundDtoMapper;
import com.easyterview.wingterview.common.util.mapper.dto.PartnerMapper;
import com.easyterview.wingterview.common.util.mapper.entity.InterviewSegmentMapper;
import com.easyterview.wingterview.common.util.mapper.entity.InterviewTimeMapper;
import com.easyterview.wingterview.interview.dto.response.*;
import com.easyterview.wingterview.interview.entity.*;
import com.easyterview.wingterview.interview.enums.Phase;
import com.easyterview.wingterview.interview.provider.*;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.provider.UserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 인터뷰 상태 흐름 관리 (라운드 진행, 인터뷰 종료 등)
@Service
@RequiredArgsConstructor
public class InterviewFlowServiceImpl implements InterviewFlowService {

    private final InterviewRepository interviewRepository;
    private final InterviewTimeRepository interviewTimeRepository;
    private final InterviewSegmentRepository interviewSegmentRepository;
    private final QuestionOptionsRepository questionOptionsRepository;
    private final QuestionHistoryRepository questionHistoryRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final InterviewProvider interviewProvider;
    private final InterviewHistoryProvider interviewHistoryProvider;
    private final InterviewTimeProvider interviewTimeProvider;
    private final InterviewSegmentProvider interviewSegmentProvider;
    private final QuestionHistoryProvider questionHistoryProvider;
    private final UserProvider userProvider;
    private final InterviewParticipantProvider interviewParticipantProvider;
    private final QuestionOptionsProvider questionOptionsProvider;

    @Override
    @Transactional
    public NextRoundDto goNextStage(String interviewId) {
        InterviewEntity interview = interviewProvider.getInterviewOrThrow(interviewId);
        InterviewStatus nextStatus = InterviewUtil.nextPhase(interview.getRound(), interview.getPhase(), interview.getIsAiInterview());

        // 1:1면접이면서 Pending이면 시간 설정
        if (!interview.getIsAiInterview() && isEnteringProgress(nextStatus)) {
            initializeNormalInterviewTime(interview);
        }

        // AI 면접이면서 Progress이면 면접이 끝나므로 마지막 segment 저장 & 인터뷰 끝 저장
        else if (interview.getIsAiInterview() && isCurrentProgress(interview)) {
            UserEntity user = userProvider.getUserOrThrow();
            InterviewHistoryEntity interviewHistory = interviewHistoryProvider.getInterviewHistoryOrThrow(user);
            InterviewTimeEntity interviewTime = interviewTimeProvider.getInterviewTimeOrThrow(interview);

            setInterviewEndAndSaveInterviewSegment(interview, interviewTime, interviewHistory);
        }

        // 새 status 저장
        interview.setPhase(nextStatus.getPhase());
        interview.setRound(nextStatus.getRound());

        // 인터뷰 관련 options, history 다 지우기(다음 분기를 위해)
        questionOptionsRepository.deleteAllByInterviewId(UUID.fromString(interviewId));
        questionHistoryRepository.deleteAllByInterviewId(UUID.fromString(interviewId));

        // 바꾼 분기 dto 리턴
        return NextRoundDtoMapper.of(nextStatus);
    }


    @Override
    @Transactional(readOnly = true)
    public Object getInterviewStatus() {

        // 유저 정보 -> 인터뷰 정보 가져오기
        UserEntity user = userProvider.getUserOrThrow();
        InterviewParticipantEntity interviewParticipant = interviewParticipantProvider.getInterviewParticipantOrThrow(user);
        InterviewEntity interview = interviewParticipant.getInterview();

        if (!interview.getIsAiInterview()) {
            return buildHumanInterviewStatusDto(user, interview, interviewParticipant);
        }

        // AI Interview
        else {
            return buildAiInterviewInfoDto(interview);
        }
    }

    @Override
    @Transactional
    public void exitInterview(String interviewId) {
        interviewRepository.deleteById(UUID.fromString(interviewId));
    }

    @Override
    public InterviewIdResponse getInterviewId(String userId) {
        Optional<InterviewParticipantEntity> interviewParticipant = interviewParticipantRepository.findByUserId(UUID.fromString(userId));
        return interviewParticipant.isPresent() ?
                InterviewIdResponse.builder()
                        .interviewId(interviewParticipant.get().getInterview().getId().toString())
                        .build()
                :
                InterviewIdResponse.builder()
                        .interviewId("")
                        .build();
    }

    // ======================== 👇 헬퍼 메서드들 👇 ========================

    private void initializeNormalInterviewTime(InterviewEntity interview) {
        InterviewTimeEntity interviewTime = InterviewTimeMapper.toEntity(20, interview);
        interviewTimeRepository.save(interviewTime);
        interview.setInterviewTime(interviewTime);
    }

    private boolean isCurrentProgress(InterviewEntity interview) {
        return interview.getPhase().getPhase().equals("progress");
    }

    private boolean isEnteringProgress(InterviewStatus nextStatus) {
        return nextStatus.getPhase().getPhase().equals("progress");
    }

    private void setInterviewEndAndSaveInterviewSegment(InterviewEntity interview, InterviewTimeEntity interviewTime, InterviewHistoryEntity interviewHistory) {
        Timestamp endAt = TimeUtil.getEndAt(interviewTime.getEndAt());
        interviewHistory.setEndAt(endAt);

        InterviewSegmentEntity interviewSegment = InterviewSegmentMapper.toEntity(interviewHistory,
                interviewSegmentProvider.getCurrentSegmentOrder(interviewHistory) + 1,
                interview,
                questionHistoryProvider.getQuestionHistoryOrThrow(interview));

        interviewSegmentRepository.save(interviewSegment);
    }

    private InterviewStatusDto buildHumanInterviewStatusDto(UserEntity user, InterviewEntity interview, InterviewParticipantEntity interviewParticipant) {
        // 상대방 정보 가져오기
        UserEntity partnerEntity = userProvider.findPartner(interview, user);

        // 남은 시간 계산
        Integer timeRemain = getTimeRemain(interview);

        // 내가 현재 인터뷰어인지 확인
        boolean isInterviewer = InterviewUtil.checkInterviewer(interviewParticipant.getRole(), interview.getRound());
        // 상대방 정보 dto
        Partner partner = PartnerMapper.of(partnerEntity);
        // 질문 정보 record
        QuestionInfo questionInfo = getQuestionInfo(interview);

        // return
        return InterviewStatusMapper.of(interview, timeRemain, isInterviewer, partner, questionInfo);
    }

    private AiInterviewInfoDto buildAiInterviewInfoDto(InterviewEntity interview) {
        // 남은 시간 계산
        Integer timeRemain = getTimeRemain(interview);

        // 질문 정보 record
        QuestionInfo questionInfo = getQuestionInfo(interview);

        // return
        return AiInterviewInfoMapper.of(interview, timeRemain, questionInfo);
    }

    private QuestionInfo getQuestionInfo(InterviewEntity interview) {
        if (!interview.getPhase().equals(Phase.PROGRESS)) {
            return new QuestionInfo(-1, "", List.of());
        }

        Optional<QuestionHistoryEntity> questionHistory = questionHistoryProvider.getQuestionHistoryOpt(interview);
        List<String> options = questionOptionsProvider.getLastOptions(interview).stream()
                .map(QuestionOptionsEntity::getOption)
                .toList();;

        return new QuestionInfo(
                        questionHistory.map(QuestionHistoryEntity::getSelectedQuestionIdx).orElse(-1),
                        questionHistory.map(QuestionHistoryEntity::getSelectedQuestion).orElse(""),
                        options);

    }

    private Integer getTimeRemain(InterviewEntity interview) {
        InterviewTimeEntity interviewTime = interviewTimeProvider.getInterviewTimeOrThrow(interview);
        return TimeUtil.getRemainTime(interviewTime.getEndAt());
    }

}
