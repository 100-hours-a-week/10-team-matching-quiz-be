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
import com.easyterview.wingterview.rabbitmq.service.RabbitMqService;
import com.easyterview.wingterview.user.entity.UserChatroomEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserChatroomRepository;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public InterviewStatusDto getInterviewStatus() {

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
            if (interview.getPhase().equals(Phase.PROGRESS)) {
                Optional<QuestionHistoryEntity> questionHistory = questionHistoryRepository.findByInterview(interview);
                Optional<QuestionOptionsEntity> questionOption = questionOptionsRepository.findTop1ByInterviewOrderByCreatedAtDesc(interview);
                if (questionOption.isPresent()) {
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
                    .currentRound(interview.getRound())
                    .currentPhase(interview.getPhase().getPhase())
                    .isInterviewer(isInterviewer)
                    .isAiInterview(interview.getIsAiInterview())
                    .partner(partner)
                    .questionIdx(questionIdx)
                    .selectedQuestion(selectedQuestion)
                    .questionOption(questionOptions)
                    .build();

        } else {
            int questionIdx = -1;
            String selectedQuestion = "";
            List<String> questionOptions = null;
            if (interview.getPhase().equals(Phase.PROGRESS)) {
                Optional<QuestionHistoryEntity> questionHistory = questionHistoryRepository.findByInterview(interview);
                Optional<QuestionOptionsEntity> questionOption = questionOptionsRepository.findTop1ByInterviewOrderByCreatedAtDesc(interview);
                if (questionOption.isPresent()) {
                    questionOptions = new ArrayList<>();
                    questionOptions.add(questionOption.get().getFirstOption());
                    questionOptions.add(questionOption.get().getSecondOption());
                    questionOptions.add(questionOption.get().getThirdOption());
                    questionOptions.add(questionOption.get().getFourthOption());
                }
                questionIdx = questionHistory.isPresent() ? questionHistory.get().getSelectedQuestionIdx() : -1;
                selectedQuestion = questionHistory.isPresent() ? questionHistory.get().getSelectedQuestion() : "";
            }

            return InterviewStatusDto.builder()
                    .interviewId(String.valueOf(interview.getId()))
                    .currentRound(interview.getRound())
                    .currentPhase(interview.getPhase().getPhase())
                    .isAiInterview(interview.getIsAiInterview())
                    .questionIdx(questionIdx)
                    .selectedQuestion(selectedQuestion)
                    .questionOption(questionOptions)
                    .build();
        }
    }

    @Override
    @Transactional
    public QuestionCreationResponseDto makeQuestion(String interviewId, QuestionCreationRequestDto dto) {
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
            QuestionOptionsEntity questionOptions = QuestionOptionsEntity.builder()
                    .firstOption(questions.get(0))
                    .secondOption(questions.get(1))
                    .thirdOption(questions.get(2))
                    .fourthOption(questions.get(3))
                    .interview(interview)
                    .build();

            questionOptions.setInterview(interview); // 연관관계 주인 설정
            interview.getQuestionOptionsList().add(questionOptions); // 양방향 연관관계 동기화
            questionOptionsRepository.save(questionOptions); // 주인 저장


            // question responsebody
            return QuestionCreationResponseDto.builder()
                    .questions(questions)
                    .build();
        } else {
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

                String selectedQuestion = interview.getQuestionHistory().getSelectedQuestion();

                String keyword = dto.getKeywords();

                FollowUpQuestionRequest request = FollowUpQuestionRequest.builder()
                        .interviewId(interviewId)
                        .selectedQuestion(selectedQuestion)
                        .keyword(keyword)
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

                QuestionOptionsEntity questionOptions = QuestionOptionsEntity.builder()
                        .firstOption(questions.get(0))
                        .secondOption(questions.get(1))
                        .thirdOption(questions.get(2))
                        .fourthOption(questions.get(3))
                        .interview(interview)
                        .build();


                questionOptionsRepository.save(questionOptions);

                log.info("✅ 꼬리질문 저장 완료: {}", questionOptions);

                return QuestionCreationResponseDto.builder()
                        .questions(questions)
                        .build();
            }

            // 3. question != null && keywords == null 꼬리질문 생성(키워드 x)
            // 4. 꼬리질문 생성(키워드 o)
            else {
                // 꼬리 질문 요청용 DTO 생성
                FollowUpQuestionRequest requestDto = FollowUpQuestionRequest.builder()
                        .interviewId(interviewId)
                        .selectedQuestion(interview.getQuestionHistory().getSelectedQuestion())
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

                QuestionOptionsEntity questionOptions = QuestionOptionsEntity.builder()
                        .firstOption(questions.get(0))
                        .secondOption(questions.get(1))
                        .thirdOption(questions.get(2))
                        .fourthOption(questions.get(3))
                        .interview(interview)
                        .build();


                questionOptionsRepository.save(questionOptions);

                log.info("✅ 꼬리질문 저장 완료: {}", questionOptions);

                return QuestionCreationResponseDto.builder()
                        .questions(questions)
                        .build();
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
                .user(user)
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
    public AiInterviewResponseDto startAiInterview() {
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);

        InterviewParticipantEntity interviewee = InterviewParticipantEntity.builder()
                .user(user)
                .role(ParticipantRole.SECOND_INTERVIEWER)
                .build();

        List<InterviewParticipantEntity> participants = List.of(interviewee);

        InterviewEntity interview = InterviewEntity.builder()
                .participants(participants)
                .isAiInterview(true)
                .build();

        interviewee.setInterview(interview);

        UUID interviewId = interviewRepository.save(interview).getId();

        return AiInterviewResponseDto.builder()
                .interviewId(String.valueOf(interviewId))
                .build();
    }
}