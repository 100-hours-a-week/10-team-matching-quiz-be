package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.common.util.InterviewStatus;
import com.easyterview.wingterview.common.util.InterviewUtil;
import com.easyterview.wingterview.common.util.TimeUtil;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.*;
import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.dto.request.QuestionSelectionRequestDto;
import com.easyterview.wingterview.interview.dto.response.InterviewStatusDto;
import com.easyterview.wingterview.interview.dto.response.NextRoundDto;
import com.easyterview.wingterview.interview.dto.response.Partner;
import com.easyterview.wingterview.interview.dto.response.QuestionCreationResponseDto;
import com.easyterview.wingterview.interview.entity.*;
import com.easyterview.wingterview.interview.enums.Phase;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final RestClient restClient;
    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final UserRepository userRepository;
    private final QuestionOptionsRepository questionOptionsRepository;
    private final QuestionHistoryRepository questionHistoryRepository;
    private final ReceivedQuestionRepository receivedQuestionRepository;
    private final MainQuestionRepository mainQuestionRepository;


    @Value("${ai.follow-up-url}")
    private String followUpUrl;

    private final String tmpUrl = "https://0e28-34-124-213-134.ngrok-free.app/interview/followup-questions";

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

        // 상대방 정보 가져오기
        InterviewParticipantEntity partnerParticipant = interview.getParticipants()
                .stream()
                .filter(i -> !i.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("상대 유저를 찾을 수 없습니다."));
        UserEntity partnerEntity = partnerParticipant.getUser();

        // 남은 시간 계산
        int timeRemain = TimeUtil.getRemainTime(interview.getPhaseAt(), InterviewStatus.builder().round(interview.getRound()).phase(interview.getPhase()).build());

        // 내가 현재 인터뷰어인지 확인
        boolean isInterviewer = InterviewUtil.checkInterviewer(interviewParticipant.getRole(), interview.getRound());

        // 상대방 정보 dto
        Partner partner = Partner.builder()
                .name(partnerEntity.getName())
                .nickname(partnerEntity.getNickname())
                .profileImageUrl(partnerEntity.getProfileImageUrl())
                .techStack(partnerEntity.getUserTechStack().stream().map(t -> t.getTechStack().getLabel()).toList())
                .jobInterest(partnerEntity.getUserJobInterest().stream().map(j -> j.getJobInterest().getLabel()).toList())
                .curriculum(partnerEntity.getCurriculum())
                .build();

        int questionIdx = -1;
        String selectedQuestion = "";
        List<String> questionOptions = null;
        if(interview.getPhase().equals(Phase.PROGRESS)){
            Optional<QuestionHistoryEntity> questionHistory = questionHistoryRepository.findByInterview(interview);
            Optional<QuestionOptionsEntity> questionOption = questionOptionsRepository.findTop1ByInterviewOrderByCreatedAtDesc(interview);
            if(questionOption.isPresent()){
                questionOptions = new ArrayList<>();
                questionOptions.add(questionOption.get().getFirstOption());
                questionOptions.add(questionOption.get().getSecondOption());
                questionOptions.add(questionOption.get().getThirdOption());
                questionOptions.add(questionOption.get().getFourthOption());
            }
            questionIdx = questionHistory.isPresent() ? questionHistory.get().getSelectedQuestionIdx() : -1;
            selectedQuestion = questionHistory.isPresent() ? questionHistory.get().getSelectedQuestion() : "";
        }



        // 인터뷰 상태 dto 반환
        return InterviewStatusDto.builder()
                .interviewId(String.valueOf(interview.getId()))
                .timeRemain(timeRemain)
                // TODO : 시간에 맞게 round, phase 계산해주기 ??
                .currentRound(interview.getRound())
                .currentPhase(interview.getPhase().getPhase())
                .isInterviewer(isInterviewer)
                .isAiInterview(interview.getIsAiInterview())
                .partner(partner)
                .questionIdx(questionIdx)
                .selectedQuestion(selectedQuestion)
                .questionOption(questionOptions)
                .build();

    }

    @Override
    @Transactional
    public QuestionCreationResponseDto makeQuestion(String interviewId, QuestionCreationRequestDto dto) {
        // 1. question == null 메인질문 생성
        if (dto.getQuestion().isEmpty()) {
            // TODO : 메인질문 중복처리를 위한 user_main_question ??
            InterviewEntity interview = interviewRepository.findById(UUID.fromString(interviewId))
                    .orElseThrow(InterviewNotFoundException::new);
            UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                    .orElseThrow(InvalidTokenException::new);

            List<String> jobInterests = user.getUserJobInterest().stream()
                    .map(j -> j.getJobInterest().name())
                    .collect(Collectors.toList());

            List<String> techStacks = user.getUserTechStack().stream()
                    .map(t -> t.getTechStack().name())
                    .collect(Collectors.toList());

            List<String> questions = mainQuestionRepository
                    .findRandomMatchingQuestions(jobInterests, techStacks).stream()
                    .map(MainQuestionEntity::getContents)
                    .toList();

            QuestionOptionsEntity questionOptions = QuestionOptionsEntity.builder()
                    .firstOption(questions.get(0))
                    .secondOption(questions.get(1))
                    .thirdOption(questions.get(2))
                    .fourthOption(questions.get(3))
                    .interview(interview)
                    .build();

            questionOptionsRepository.save(questionOptions);

            return QuestionCreationResponseDto.builder()
                    .questions(questions)
                    .build();
        } else {
            InterviewEntity interview = interviewRepository.findById(UUID.fromString(interviewId)).orElseThrow(InterviewNotFoundException::new);
            // 2. QuestionHistory가 있으면서 이전 Question이 똑같음 -> 꼬리질문 재생성한거임 -> passed Question 넣어서 AI에 보내기. + passed Question 누적
            if (interview.getQuestionHistory() != null && dto.getQuestion().equals(interview.getQuestionHistory().getSelectedQuestion())) {
                // 최근 20개를 가져와서 passed question 구성하기
                List<String> passedQuestions = questionOptionsRepository.findTop5ByOrderByCreatedAtDesc()
                        .stream()
                        .flatMap(q -> Stream.of(
                                q.getFirstOption(),
                                q.getSecondOption(),
                                q.getThirdOption(),
                                q.getFourthOption()
                        ))
                        .collect(Collectors.toList());

                // AI에 요청 보내기
                Map<String, Object> jsonBody = new HashMap<>();
                jsonBody.put("interview_id", interviewId);
                jsonBody.put("selected_question", interview.getQuestionHistory().getSelectedQuestion());
                jsonBody.put("keyword", dto.getKeywords()); // null 가능
                jsonBody.put("passed_questions", passedQuestions); // List<String>

                Map<String, Object> response = restClient.post()
                        .uri(tmpUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jsonBody)
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});

                // followup_questions 추출 및 안전한 캐스팅
                Object rawOptions = response.get("followup_questions");
                if (!(rawOptions instanceof List<?> rawList)) {
                    throw new RuntimeException("응답 형식이 예상과 다릅니다: followup_questions");
                }

                List<String> options = rawList.stream().map(Object::toString).toList();

                // questionOptions 저장하기
                QuestionOptionsEntity questionOptions = QuestionOptionsEntity.builder()
                        .firstOption(options.get(0))
                        .secondOption(options.get(1))
                        .thirdOption(options.get(2))
                        .fourthOption(options.get(3))
                        .interview(interview)
                        .build();

                questionOptionsRepository.save(questionOptions);

                // question responsebody
                return QuestionCreationResponseDto.builder()
                        .questions(options)
                        .build();
            }

            // 3. question != null && keywords == null 꼬리질문 생성(키워드 x)
            // 4. 꼬리질문 생성(키워드 o)
            else {
                // AI에 요청 보내기
                Map<String, Object> jsonBody = new HashMap<>();
                jsonBody.put("interview_id", interviewId);
                jsonBody.put("selected_question", interview.getQuestionHistory().getSelectedQuestion());
                jsonBody.put("keyword", dto.getKeywords());

                Map<String, Object> response = restClient.post()
                        .uri(tmpUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jsonBody)
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});

                // questionOptions 저장하기
                Object rawOptions = response.get("followup_questions");
                if (rawOptions instanceof List<?> rawList) {
                    List<String> options = rawList.stream().map(Object::toString).toList();
                    QuestionOptionsEntity questionOptions = QuestionOptionsEntity.builder()
                            .firstOption(options.get(0))
                            .secondOption(options.get(1))
                            .thirdOption(options.get(2))
                            .fourthOption(options.get(3))
                            .interview(interview)
                            .build();

                    questionOptionsRepository.save(questionOptions);

                    // question responsebody
                    return QuestionCreationResponseDto.builder()
                            .questions(options)
                            .build();
                } else {
                    throw new RuntimeException("응답이 예상과 다릅니다: followup_questions");
                }
            }
        }
    }

    @Transactional
    @Override
    public void selectQuestion(String interviewId, QuestionSelectionRequestDto dto) {

        InterviewEntity interview = interviewRepository.findById(UUID.fromString(interviewId))
                .orElseThrow(InterviewNotFoundException::new);
        QuestionOptionsEntity questionOptions = questionOptionsRepository.findTop1ByInterviewOrderByCreatedAtDesc(interview)
                .orElseThrow(QuestionOptionNotFoundException::new);
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);

        // 선택한 질문 골라내기
        String selectedQuestion = switch (dto.getSelectedIdx()) {
            case 1 -> questionOptions.getFirstOption();
            case 2 -> questionOptions.getSecondOption();
            case 3 -> questionOptions.getThirdOption();
            case 4 -> questionOptions.getFourthOption();
            default -> throw new IllegalArgumentException("선택 인덱스는 1~4 사이여야 합니다.");
        };

        // 질문 History 덮어쓰기
        Optional<QuestionHistoryEntity> oldQuestionHistoryOpt = questionHistoryRepository.findByInterview(interview);
        if(oldQuestionHistoryOpt.isEmpty()){
            questionHistoryRepository.save(QuestionHistoryEntity.builder()
                            .selectedQuestionIdx(1)
                            .selectedQuestion(selectedQuestion)
                            .interview(interview)
                    .build()
            );
            receivedQuestionRepository.save(ReceivedQuestionEntity.builder()
                            .contents(selectedQuestion)
                            .receivedAt(Timestamp.valueOf(LocalDateTime.now()))
                            .user(user)
                    .build()
            );
            log.info("*************** 1번 질문 생성 완료");
        }
        else{
            QuestionHistoryEntity oldQuestionHistory = oldQuestionHistoryOpt.get();
            oldQuestionHistory.setSelectedQuestionIdx(oldQuestionHistory.getSelectedQuestionIdx()+1);
            oldQuestionHistory.setSelectedQuestion(selectedQuestion);
            questionHistoryRepository.save(oldQuestionHistory);
            log.info("*************** {}번 질문 생성 완료",oldQuestionHistory.getSelectedQuestionIdx());
        }

        // 그 인터뷰에 대한 option들(지나친 질문 목록) 삭제
        questionOptionsRepository.deleteAllByInterview(interview);
    }
}
