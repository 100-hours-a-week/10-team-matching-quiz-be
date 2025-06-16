package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.chat.entity.ChatEntity;
import com.easyterview.wingterview.chat.entity.ChatroomEntity;
import com.easyterview.wingterview.chat.repository.ChatRepository;
import com.easyterview.wingterview.chat.repository.ChatroomRepository;
import com.easyterview.wingterview.common.util.InterviewStatus;
import com.easyterview.wingterview.common.util.InterviewUtil;
import com.easyterview.wingterview.common.util.TimeUtil;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.*;
import com.easyterview.wingterview.interview.dto.request.*;
import com.easyterview.wingterview.interview.dto.response.*;
import com.easyterview.wingterview.interview.entity.*;
import com.easyterview.wingterview.interview.enums.ParticipantRole;
import com.easyterview.wingterview.interview.enums.Phase;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.rabbitmq.consumer.FeedbackConsumer;
import com.easyterview.wingterview.rabbitmq.service.RabbitMqService;
import com.easyterview.wingterview.user.entity.RecordingEntity;
import com.easyterview.wingterview.user.entity.UserChatroomEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.RecordRepository;
import com.easyterview.wingterview.user.repository.UserChatroomRepository;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final RestTemplate restTemplate;
    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final UserRepository userRepository;
    private final QuestionOptionsRepository questionOptionsRepository;
    private final QuestionHistoryRepository questionHistoryRepository;
    private final ReceivedQuestionRepository receivedQuestionRepository;
    private final MainQuestionRepository mainQuestionRepository;
    private final UserChatroomRepository userChatroomRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatRepository chatRepository;
    private final InterviewTimeRepository interviewTimeRepository;
    private final InterviewHistoryRepository interviewHistoryRepository;
    private final InterviewSegmentRepository interviewSegmentRepository;
    private final RecordRepository recordRepository;
    private final FeedbackConsumer feedbackConsumer;

    private final RabbitMqService rabbitMqService;


    @Value("${ai.follow-up-url}")
    private String followUpUrl;

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
            if (!interview.getIsAiInterview() && nextStatus.getPhase().getPhase().equals("progress")) {
                InterviewTimeEntity interviewTime = InterviewTimeEntity.builder()
                        .endAt(Timestamp.valueOf(LocalDateTime.now().plusMinutes(20)))
                        .build();

                interviewTime.setInterview(interview);
                interview.setInterviewTime(interviewTime);
            } else if (interview.getIsAiInterview() && interview.getPhase().getPhase().equals("progress")) {
                UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                        .orElseThrow(InvalidTokenException::new);
                InterviewHistoryEntity interviewHistory = interviewHistoryRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()).orElseThrow(InterviewNotFoundException::new);
                InterviewTimeEntity interviewTime = interviewTimeRepository.findByInterview(interviewOpt.get()).orElseThrow(InterviewNotFoundException::new);
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                Timestamp originalEndAt = interviewTime.getEndAt();
                interviewHistory.setEndAt(now.getTime() > originalEndAt.getTime() ? originalEndAt : now);

                InterviewSegmentEntity interviewSegment = InterviewSegmentEntity.builder()
                        .interviewHistory(interviewHistory)
                        .segmentOrder(interviewSegmentRepository.countByInterviewHistory(interviewHistory) + 1)
                        .fromTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), interview.getQuestionHistory().getCreatedAt()))
                        .toTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), Timestamp.valueOf(LocalDateTime.now())))
                        .selectedQuestion(questionHistoryRepository.findByInterview(interview).get().getSelectedQuestion())
                        .build();

                interviewSegmentRepository.save(interviewSegment);
            }
            interview.setPhase(nextStatus.getPhase());
            interview.setRound(nextStatus.getRound());

            // 인터뷰 관련 options, history 다 지우기(다음 분기를 위해)
            questionOptionsRepository.deleteAllByInterview(interview);
            questionHistoryRepository.deleteAllByInterviewId(UUID.fromString(interviewId));

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
    public Object getInterviewStatus() {

        // 유저 정보 -> 인터뷰 정보 가져오기
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);
        InterviewParticipantEntity interviewParticipant = interviewParticipantRepository.findByUser(user)
                .orElseThrow(UserNotParticipatedException::new);
        InterviewEntity interview = interviewParticipant.getInterview();

        if (!interview.getIsAiInterview()) {
            // 상대방 정보 가져오기
            InterviewParticipantEntity partnerParticipant = interview.getParticipants()
                    .stream()
                    .filter(i -> !i.getUser().getId().equals(user.getId()))
                    .findFirst()
                    .orElseThrow(UserNotFoundException::new);
            UserEntity partnerEntity = partnerParticipant.getUser();

            Optional<InterviewTimeEntity> interviewTime = interviewTimeRepository.findByInterview(interview);
            Integer timeRemain = interviewTime.map(interviewTimeEntity -> TimeUtil.getRemainTime(interviewTimeEntity.getEndAt())).orElse(null);

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
            if (interview.getPhase().equals(Phase.PROGRESS)) {
                Optional<QuestionHistoryEntity> questionHistory = questionHistoryRepository.findByInterview(interview);
                List<QuestionOptionsEntity> questionOption = questionOptionsRepository.findTop4ByInterviewOrderByCreatedAtDesc(interview);
                if (!questionOption.isEmpty()) {
                    questionOptions = new ArrayList<>(questionOption.stream().map(QuestionOptionsEntity::getOption).toList());
                }
                questionIdx = questionHistory.isPresent() ? questionHistory.get().getSelectedQuestionIdx() : -1;
                selectedQuestion = questionHistory.isPresent() ? questionHistory.get().getSelectedQuestion() : "";
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
        // AI Interview
        else {
            int questionIdx = -1;
            String question = "";
            Optional<InterviewTimeEntity> interviewTime = interviewTimeRepository.findByInterview(interview);
            Integer timeRemain = interviewTime.map(interviewTimeEntity -> TimeUtil.getRemainTime(interviewTimeEntity.getEndAt())).orElse(null);

            if (interview.getPhase().equals(Phase.PROGRESS)) {
                Optional<QuestionHistoryEntity> questionHistory = questionHistoryRepository.findByInterview(interview);
                questionIdx = questionHistory.isPresent() ? questionHistory.get().getSelectedQuestionIdx() : -1;
                question = questionHistory.isPresent() ? questionHistory.get().getSelectedQuestion() : "";
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
    }

    @Override
    @Transactional
    public Object makeQuestion(String interviewId, QuestionCreationRequestDto dto) {
        log.info("*******Make Question 로그********");
        log.info(dto.toString());

        InterviewEntity interview = interviewRepository.findById(UUID.fromString(interviewId))
                .orElseThrow(InterviewNotFoundException::new);
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);

        // 1. question == null 메인질문 생성
        if (dto.getQuestion().isEmpty()) {
            List<String> questions;
            if (interview.getIsAiInterview()) {
                // 희망 직무, 테크스택 관련 메인 질문 뽑아오기
                List<String> jobInterests = user.getUserJobInterest().stream()
                        .map(j -> j.getJobInterest().name())
                        .toList();


                List<String> techStacks = user.getUserTechStack().stream()
                        .map(t -> t.getTechStack().name())
                        .toList();

                questions = mainQuestionRepository
                        .findRandomMatchingQuestions(jobInterests, techStacks).stream()
                        .map(MainQuestionEntity::getContents)
                        .toList();

                QuestionHistoryEntity questionHistory = interview.getQuestionHistory();
                if (questionHistory == null) {
                    questionHistoryRepository.save(QuestionHistoryEntity.builder()
                            .interview(interview)
                            .selectedQuestion(questions.getFirst())
                            .selectedQuestionIdx(1)
                            .build()
                    );
                } else {
                    // 아마 마지막 질문 녹음은 따로 처리해야할듯
                    InterviewHistoryEntity interviewHistory = interviewHistoryRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()).orElseThrow(InterviewNotFoundException::new);
                    InterviewSegmentEntity interviewSegment = InterviewSegmentEntity.builder()
                            .interviewHistory(interviewHistory)
                            .segmentOrder(interviewSegmentRepository.countByInterviewHistory(interviewHistory) + 1)
                            .fromTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), interview.getQuestionHistory().getCreatedAt()))
                            .toTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), Timestamp.valueOf(LocalDateTime.now())))
                            .selectedQuestion(questionHistoryRepository.findByInterview(interview).get().getSelectedQuestion())
                            .build();

                    interviewSegmentRepository.save(interviewSegment);

                    Integer questionIdx = questionHistory.getSelectedQuestionIdx();
                    questionHistory.setSelectedQuestion(questions.getFirst());
                    questionHistory.setSelectedQuestionIdx(questionIdx + 1);
                    questionHistoryRepository.save(questionHistory);

                }


                receivedQuestionRepository.save(ReceivedQuestionEntity.builder()
                        .contents(questions.getFirst())
                        .receivedAt(Timestamp.valueOf(LocalDateTime.now()))
                        .user(user)
                        .build()
                );

                return AiQuestionCreationResponseDto.builder()
                        .question(questions.getFirst())
                        .build();
            } else {

                // 다른 참가자 추출하기(면접관이 아닌 면접자와 관련된 기술스택, 희망직무)
                List<InterviewParticipantEntity> otherParticipantList = interviewParticipantRepository.findByInterview(interview)
                        .stream().filter(i -> !i.getUser().getId().equals(user.getId())).toList();
                UserEntity otherUser = otherParticipantList.getFirst().getUser();


                // 희망 직무, 테크스택 관련 메인 질문 뽑아오기
                List<String> jobInterests = otherUser.getUserJobInterest().stream()
                        .map(j -> j.getJobInterest().name())
                        .toList();


                List<String> techStacks = otherUser.getUserTechStack().stream()
                        .map(t -> t.getTechStack().name())
                        .toList();

                questions = mainQuestionRepository
                        .findRandomMatchingQuestions(jobInterests, techStacks).stream()
                        .map(MainQuestionEntity::getContents)
                        .toList();

            }

            // questionOptions 저장하기
            questions.forEach(q -> {
                QuestionOptionsEntity questionOption = QuestionOptionsEntity.builder().option(q).build();
                questionOption.setInterview(interview);
                interview.getQuestionOptions().add(questionOption);// 양방향 연관관계 동기화
                questionOptionsRepository.save(questionOption);
            });

            // question responsebody
            return QuestionCreationResponseDto.builder()
                    .questions(questions)
                    .build();
        } else {
            // 2. QuestionHistory가 있으면서 이전 Question이 똑같음 -> 꼬리질문 재생성한거임 -> passed Question 넣어서 AI에 보내기. + passed Question 누적
            if (interview.getQuestionHistory() != null && dto.getQuestion().equals(interview.getQuestionHistory().getSelectedQuestion())) {
                // 최근 20개를 가져와서 passed question 구성하기
                List<String> passedQuestions = questionOptionsRepository.findTop20ByOrderByCreatedAtDesc()
                        .stream()
                        .map(QuestionOptionsEntity::getOption)
                        .toList();

                FollowUpQuestionRequest request = FollowUpQuestionRequest.builder()
                        .interviewId(interviewId)
                        .selectedQuestion(dto.getQuestion())
                        .keyword(dto.getKeywords())
                        .passedQuestions(passedQuestions.isEmpty() ? null : passedQuestions)
                        .build();

                /* legacy code */

//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_JSON);
//
//                HttpEntity<FollowUpQuestionRequest> entity = new HttpEntity<>(request, headers);
//
//                ResponseEntity<FollowUpQuestionResponseDto> response = restTemplate.postForEntity(
//                        followUpUrl,
//                        entity,
//                        FollowUpQuestionResponseDto.class
//                );
//
//                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
//                    throw new RuntimeException("❌ 꼬리질문 생성 서버 응답 실패");
//                }


                FollowUpQuestionResponseDto response = rabbitMqService.sendFollowUpBlocking(request);
                List<String> questions = response.getFollowupQuestions();

                if (!interview.getIsAiInterview()) {
                    // questionOptions 저장하기
                    questions.forEach(q -> {
                        QuestionOptionsEntity questionOption = QuestionOptionsEntity.builder().option(q).build();
                        questionOption.setInterview(interview);
                        interview.getQuestionOptions().add(questionOption);// 양방향 연관관계 동기화
                        questionOptionsRepository.save(questionOption);
                    });

                    return QuestionCreationResponseDto.builder()
                            .questions(questions)
                            .build();
                }

                // ai라면 생성과 동시에 history에 저장해야함. -> select가 분리되어 있지 않기때문
                QuestionHistoryEntity questionHistory = interview.getQuestionHistory();
                if (questionHistory == null) {
                    questionHistoryRepository.save(QuestionHistoryEntity.builder()
                            .interview(interview)
                            .selectedQuestion(questions.getFirst())
                            .selectedQuestionIdx(1)
                            .build()
                    );


                } else {
                    InterviewHistoryEntity interviewHistory = interviewHistoryRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()).orElseThrow(InterviewNotFoundException::new);
                    InterviewSegmentEntity interviewSegment = InterviewSegmentEntity.builder()
                            .interviewHistory(interviewHistory)
                            .segmentOrder(interviewSegmentRepository.countByInterviewHistory(interviewHistory) + 1)
                            .fromTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), interview.getQuestionHistory().getCreatedAt()))
                            .toTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), Timestamp.valueOf(LocalDateTime.now())))
                            .selectedQuestion(questionHistoryRepository.findByInterview(interview).get().getSelectedQuestion())
                            .build();

                    interviewSegmentRepository.save(interviewSegment);

                    Integer questionIdx = questionHistory.getSelectedQuestionIdx();
                    questionHistory.setSelectedQuestion(questions.getFirst());
                    questionHistory.setSelectedQuestionIdx(questionIdx + 1);
                    questionHistoryRepository.save(questionHistory);
                }


                receivedQuestionRepository.save(ReceivedQuestionEntity.builder()
                        .contents(questions.getFirst())
                        .receivedAt(Timestamp.valueOf(LocalDateTime.now()))
                        .user(user)
                        .build()
                );

                return AiQuestionCreationResponseDto.builder()
                        .question(questions.getFirst())
                        .build();
            }

            // 3. question != null && keywords == null 꼬리질문 생성(키워드 x)
            // 4. 꼬리질문 생성(키워드 o)
            else {
                // 꼬리 질문 요청용 DTO 생성
                FollowUpQuestionRequest requestDto = FollowUpQuestionRequest.builder()
                        .interviewId(interviewId)
                        .selectedQuestion(dto.getQuestion())
                        .keyword(dto.getKeywords())
                        .build();

                /* legacy code */

//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_JSON);
//
//                HttpEntity<FollowUpQuestionRequest> entity = new HttpEntity<>(requestDto, headers);
//
//                ResponseEntity<FollowUpQuestionResponseDto> response = restTemplate.postForEntity(
//                        followUpUrl,
//                        entity,
//                        FollowUpQuestionResponseDto.class
//                );
//
//                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
//                    throw new RuntimeException("❌ 꼬리질문 생성 서버 응답 실패");
//                }

                FollowUpQuestionResponseDto response = rabbitMqService.sendFollowUpBlocking(requestDto);

                List<String> questions = response.getFollowupQuestions();

                if (!interview.getIsAiInterview()) {
                    // questionOptions 저장하기
                    questions.forEach(q -> {
                        QuestionOptionsEntity questionOption = QuestionOptionsEntity.builder().option(q).build();
                        questionOption.setInterview(interview);
                        interview.getQuestionOptions().add(questionOption);// 양방향 연관관계 동기화
                    });

                    return QuestionCreationResponseDto.builder()
                            .questions(questions)
                            .build();
                }


                InterviewHistoryEntity interviewHistory = interviewHistoryRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()).orElseThrow(InterviewNotFoundException::new);
                InterviewSegmentEntity interviewSegment = InterviewSegmentEntity.builder()
                        .interviewHistory(interviewHistory)
                        .segmentOrder(interviewSegmentRepository.countByInterviewHistory(interviewHistory) + 1)
                        .fromTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), interview.getQuestionHistory().getCreatedAt()))
                        .toTime(TimeUtil.getTime(interview.getInterviewTime().getStartAt(), Timestamp.valueOf(LocalDateTime.now())))
                        .selectedQuestion(questionHistoryRepository.findByInterview(interview).get().getSelectedQuestion())
                        .build();

                interviewSegmentRepository.save(interviewSegment);

                // ai라면 생성과 동시에 history에 저장해야함. -> select가 분리되어 있지 않기때문
                QuestionHistoryEntity questionHistory = interview.getQuestionHistory();
                if (questionHistory == null) {
                    questionHistoryRepository.save(QuestionHistoryEntity.builder()
                            .interview(interview)
                            .selectedQuestion(questions.getFirst())
                            .selectedQuestionIdx(1)
                            .build()
                    );
                } else {
                    Integer questionIdx = questionHistory.getSelectedQuestionIdx();
                    questionHistory.setSelectedQuestion(questions.getFirst());
                    questionHistory.setSelectedQuestionIdx(questionIdx + 1);
                    questionHistoryRepository.save(questionHistory);

                }

                receivedQuestionRepository.save(ReceivedQuestionEntity.builder()
                        .contents(questions.getFirst())
                        .receivedAt(Timestamp.valueOf(LocalDateTime.now()))
                        .user(user)
                        .build()
                );

                return AiQuestionCreationResponseDto.builder()
                        .question(questions.getFirst())
                        .build();
            }
        }
    }

    @Transactional
    @Override
    public void selectQuestion(String interviewId, QuestionSelectionRequestDto dto) {

        InterviewEntity interview = interviewRepository.findById(UUID.fromString(interviewId))
                .orElseThrow(InterviewNotFoundException::new);
        List<QuestionOptionsEntity> questionOptions = questionOptionsRepository.findTop4ByInterviewOrderByCreatedAtDesc(interview);

        // 선택한 질문 골라내기
        String selectedQuestion = questionOptions.get(dto.getSelectedIdx()).getOption();

        // 질문 History 덮어쓰기
        Optional<QuestionHistoryEntity> oldQuestionHistoryOpt = questionHistoryRepository.findByInterview(interview);

        // 1. 원래 history 없었다면 questionIdx를 1로 설정
        if (oldQuestionHistoryOpt.isEmpty()) {
            questionHistoryRepository.save(QuestionHistoryEntity.builder()
                    .selectedQuestionIdx(1)
                    .selectedQuestion(selectedQuestion)
                    .interview(interview)
                    .build()
            );
            log.info("*************** 1번 질문 생성 완료");
        }
        // 2. 원래 있었다면 questionIdx + 1로 설정
        else {
            QuestionHistoryEntity oldQuestionHistory = oldQuestionHistoryOpt.get();
            oldQuestionHistory.setSelectedQuestionIdx(oldQuestionHistory.getSelectedQuestionIdx() + 1);
            oldQuestionHistory.setSelectedQuestion(selectedQuestion);
            questionHistoryRepository.save(oldQuestionHistory);

            log.info("*************** {}번 질문 생성 완료", oldQuestionHistory.getSelectedQuestionIdx());
        }

        // 받은 질문 목록 테이블에 저장하기
        receivedQuestionRepository.save(ReceivedQuestionEntity.builder()
                .contents(selectedQuestion)
                .receivedAt(Timestamp.valueOf(LocalDateTime.now()))
                .user(interview.getParticipants().stream().filter(participant ->
                        !participant.getUser().getId().equals(UUIDUtil.getUserIdFromToken())).toList().getFirst().getUser())
                .build()
        );

        // 그 인터뷰에 대한 option들(지나친 질문 목록) 삭제
        questionOptionsRepository.deleteAllByInterview(interview);
    }

    @Transactional
    @Override
    public void sendFeedback(String interviewId, FeedbackRequestDto dto) {
        // 1. 문자열 인터뷰 ID를 UUID로 변환
        UUID interviewUUID = UUID.fromString(interviewId);

        // 2. 인터뷰 정보 조회 (없으면 예외 발생)
        InterviewEntity interview = interviewRepository.findById(interviewUUID)
                .orElseThrow(InterviewNotFoundException::new);

        // 3. 현재 로그인된 사용자 조회
        UserEntity currentUser = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);

        // 4. 상대방 유저 찾기 (현재 유저와 ID가 다른 참여자)
        UserEntity otherUser = interview.getParticipants().stream()
                .map(InterviewParticipantEntity::getUser)
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("상대 유저를 찾을 수 없습니다."));

        ChatroomEntity chatRoom;

        // 5. 인터뷰 ID 기준으로 연결된 채팅방이 이미 있는지 확인
        List<UserChatroomEntity> existingUserChatrooms = userChatroomRepository.findAllByInterviewId(interviewUUID);
        if (!existingUserChatrooms.isEmpty()) {
            // 6. 이미 채팅방이 있다면 재사용
            chatRoom = existingUserChatrooms.getFirst().getChatroom();
        } else {
            // 7. 없으면 새로운 채팅방 생성
            chatRoom = ChatroomEntity.builder()
                    .title(currentUser.getName() + "와 " + otherUser.getName() + "의 대화방")
                    .build();
            chatroomRepository.save(chatRoom); // ID 부여를 위해 먼저 저장

            // 8. 새 채팅방을 각 사용자에게 매핑 (UserChatroomEntity)
            userChatroomRepository.save(UserChatroomEntity.builder()
                    .chatroom(chatRoom)
                    .interviewId(interviewUUID)
                    .user(currentUser)
                    .build());

            userChatroomRepository.save(UserChatroomEntity.builder()
                    .chatroom(chatRoom)
                    .interviewId(interviewUUID)
                    .user(otherUser)
                    .build());
        }

        // 9. 피드백 메시지 구성 ("내용\n(점수: 4.5)" 형태)
        String message = String.format("%s\n(점수: %.1f)", dto.getFeedback(), dto.getScore());

        // 10. ChatEntity 생성 및 저장
        ChatEntity newChat = ChatEntity.builder()
                .chatroom(chatRoom)
                .contents(message)
                .sender(currentUser)
                .build();

        chatRepository.save(newChat);
    }

    @Override
    @Transactional
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


    // AI 피드백 request API 필요함
    // feedback requested를 true로 바꿔준다.
    @Override
    @Transactional
    public void requestSttFeedback(String userId) {
        InterviewHistoryEntity interviewHistory = interviewHistoryRepository.findFirstByUserIdOrderByCreatedAtDesc(UUID.fromString(userId)).orElseThrow(InterviewNotFoundException::new);
        interviewHistory.setIsFeedbackRequested(true);
        RecordingEntity recordingEntity = recordRepository.findByInterviewHistoryId(interviewHistory.getId()).orElseThrow(InterviewNotFoundException::new);

        List<QuestionSegment> questionSegments = interviewHistory.getSegments().stream()
                .map(s -> {
                    return
                    QuestionSegment.builder()
                            .segmentId(s.getId().toString())
                            .startTime(s.getFromTime())
                            .endTime(s.getToTime())
                            .question(s.getSelectedQuestion())
                            .build();
                }).toList();



        rabbitMqService.sendSTTFeedbackRequest(STTFeedbackRequestDto.builder()
                .questionLists(questionSegments)
                .recordingUrl(recordingEntity.getUrl())
                .build());
    }

    @RabbitListener(queues = "feedback.response.queue")
    public void handleFeedbackResponse(FeedbackResponseDto responseDto) {
        log.info("📥 복습 퀴즈 생성 응답 수신: {}", responseDto);
        feedbackConsumer.consumeFeedback(responseDto);
    }
}

