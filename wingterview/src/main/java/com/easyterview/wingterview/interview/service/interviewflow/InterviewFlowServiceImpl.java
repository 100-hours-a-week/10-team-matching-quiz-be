package com.easyterview.wingterview.interview.service.interviewflow;

import com.easyterview.wingterview.common.util.InterviewStatus;
import com.easyterview.wingterview.common.util.InterviewUtil;
import com.easyterview.wingterview.common.util.TimeUtil;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.*;
import com.easyterview.wingterview.interview.dto.response.*;
import com.easyterview.wingterview.interview.entity.*;
import com.easyterview.wingterview.interview.enums.Phase;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 인터뷰 상태 흐름 관리 (라운드 진행, 인터뷰 종료 등)
@Service
@RequiredArgsConstructor
public class InterviewFlowServiceImpl implements InterviewFlowService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final InterviewHistoryRepository interviewHistoryRepository;
    private final InterviewTimeRepository interviewTimeRepository;
    private final InterviewSegmentRepository interviewSegmentRepository;
    private final QuestionOptionsRepository questionOptionsRepository;
    private final QuestionHistoryRepository questionHistoryRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;

    @Override
    @Transactional
    public NextRoundDto goNextStage(String interviewId) {
        InterviewEntity interview = interviewRepository.findById(UUID.fromString(interviewId)).orElseThrow(InterviewNotFoundException::new);
        InterviewStatus nextStatus = InterviewUtil.nextPhase(interview.getRound(), interview.getPhase(), interview.getIsAiInterview());
        // 1:1면접이면서 Pending이면 시간 설정
        if (!interview.getIsAiInterview() && isEnteringProgress(nextStatus)) {
            initializeNormalInterviewTime(interview);
        }

        // AI 면접이면서 Progress이면 면접이 끝나므로 마지막 segment 저장 & 인터뷰 끝 저장
        else if (interview.getIsAiInterview() && isCurrentProgress(interview)) {
            UserEntity user = userRepository
                    .findById(UUIDUtil.getUserIdFromToken())
                    .orElseThrow(InvalidTokenException::new);
            InterviewHistoryEntity interviewHistory = interviewHistoryRepository
                    .findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                    .orElseThrow(InterviewNotFoundException::new);
            InterviewTimeEntity interviewTime = interviewTimeRepository
                    .findByInterview(interview)
                    .orElseThrow(InterviewNotFoundException::new);

            setInterviewEndAndSaveInterviewSegment(interview, interviewTime, interviewHistory);

        }

        // 새 status 저장
        interview.setPhase(nextStatus.getPhase());
        interview.setRound(nextStatus.getRound());

        // 인터뷰 관련 options, history 다 지우기(다음 분기를 위해)
        questionOptionsRepository.deleteAllByInterviewId(UUID.fromString(interviewId));
        questionHistoryRepository.deleteAllByInterviewId(UUID.fromString(interviewId));

        // 바꾼 분기 dto 리턴
        return NextRoundDto.builder()
                .currentPhase(nextStatus.getPhase().getPhase())
                .currentRound(nextStatus.getRound())
                .build();
    }


    @Override
    @Transactional
    public Object getInterviewStatus() {

        // 유저 정보 -> 인터뷰 정보 가져오기
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);
        InterviewParticipantEntity interviewParticipant = interviewParticipantRepository.findByUser(user)
                .orElseThrow(UserNotParticipatedException::new);
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
        InterviewEntity interview = interviewRepository.findById(UUID.fromString(interviewId))
                .orElseThrow(InterviewNotFoundException::new);

        interviewRepository.delete(interview);
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


    private void initializeNormalInterviewTime(InterviewEntity interview) {
        InterviewTimeEntity interviewTime = InterviewTimeEntity.builder()
                .endAt(Timestamp.valueOf(LocalDateTime.now().plusMinutes(20)))
                .interview(interview)
                .build();

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
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp originalEndAt = interviewTime.getEndAt();
        Timestamp endAt = now.after(originalEndAt) ? originalEndAt : now;
        interviewHistory.setEndAt(endAt);

        InterviewSegmentEntity interviewSegment = InterviewSegmentEntity.builder()
                .interviewHistory(interviewHistory)
                .segmentOrder(interviewSegmentRepository.countByInterviewHistory(interviewHistory) + 1)
                .fromTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), interview.getQuestionHistory().getCreatedAt()))
                .toTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), endAt))
                .selectedQuestion(questionHistoryRepository.findByInterview(interview).orElseThrow(QuestionOptionNotFoundException::new).getSelectedQuestion())
                .build();

        interviewSegmentRepository.save(interviewSegment);
    }

    private InterviewStatusDto buildHumanInterviewStatusDto(UserEntity user, InterviewEntity interview, InterviewParticipantEntity interviewParticipant) {
        // 상대방 정보 가져오기
        UserEntity partnerEntity = getPartnerParticipant(user, interview).getUser();

        // 남은 시간 계산
        Integer timeRemain = interviewTimeRepository.findByInterview(interview)
                .map(t -> TimeUtil.getRemainTime(t.getEndAt()))
                .orElse(null);

        // 내가 현재 인터뷰어인지 확인
        boolean isInterviewer = InterviewUtil.checkInterviewer(interviewParticipant.getRole(), interview.getRound());

        // 상대방 정보 dto
        Partner partner = Partner.fromEntity(partnerEntity);

        // 질문 정보
        int questionIdx = -1;
        String selectedQuestion = "";
        List<String> questionOptions = null;
        if (interview.getPhase().equals(Phase.PROGRESS)) {
            QuestionInfo questionInfo = getQuestionInfo(interview);
            questionIdx = questionInfo.questionIdx();
            selectedQuestion = questionInfo.selectedQuestion();
            questionOptions = questionInfo.questionOptions();
        }

        // 인터뷰 상태 dto 반환
        return InterviewStatusDto.builder()
                .interviewId(String.valueOf(interview.getId()))
                .timeRemain(timeRemain)
                .currentRound(interview.getRound())
                .currentPhase(interview.getPhase().getPhase())
                .isInterviewer(isInterviewer)
                .isAiInterview(false)
                .partner(partner)
                .questionIdx(questionIdx)
                .selectedQuestion(selectedQuestion)
                .questionOption(questionOptions)
                .build();
    }

    private AiInterviewInfoDto buildAiInterviewInfoDto(InterviewEntity interview) {
        // 남은 시간 계산
        Integer timeRemain = interviewTimeRepository.findByInterview(interview)
                .map(t -> TimeUtil.getRemainTime(t.getEndAt()))
                .orElse(null);

        // 질문 정보
        int questionIdx = -1;
        String question = "";
        if (interview.getPhase() == Phase.PROGRESS) {
            QuestionInfo questionInfo = getQuestionInfo(interview);
            questionIdx = questionInfo.questionIdx();
            question = questionInfo.selectedQuestion();
        }

        return AiInterviewInfoDto.builder()
                .InterviewId(String.valueOf(interview.getId()))
                .currentPhase(interview.getPhase().getPhase())
                .isAiInterview(true)
                .question(question)
                .questionIdx(questionIdx)
                .timeRemain(timeRemain)
                .build();
    }

    private QuestionInfo getQuestionInfo(InterviewEntity interview) {
        Optional<QuestionHistoryEntity> questionHistory = questionHistoryRepository.findByInterview(interview);
        List<String> options = questionOptionsRepository.findTop4ByInterviewOrderByCreatedAtDesc(interview)
                .stream()
                .map(QuestionOptionsEntity::getOption)
                .toList();

        return new QuestionInfo(
                questionHistory.map(QuestionHistoryEntity::getSelectedQuestionIdx).orElse(-1),
                questionHistory.map(QuestionHistoryEntity::getSelectedQuestion).orElse(""),
                options
        );
    }

    private record QuestionInfo(int questionIdx, String selectedQuestion, List<String> questionOptions) {}

    private InterviewParticipantEntity getPartnerParticipant(UserEntity user, InterviewEntity interview) {
        return interview.getParticipants()
                .stream()
                .filter(i -> !i.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(UserNotFoundException::new);
    }
}
