package com.easyterview.wingterview.interview.service.question;

import com.easyterview.wingterview.common.util.TimeUtil;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.global.exception.InvalidTokenException;
import com.easyterview.wingterview.global.exception.QuestionOptionNotFoundException;
import com.easyterview.wingterview.interview.dto.request.FollowUpQuestionRequest;
import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.dto.request.QuestionSelectionRequestDto;
import com.easyterview.wingterview.interview.dto.response.AiQuestionCreationResponseDto;
import com.easyterview.wingterview.interview.dto.response.FollowUpQuestionResponseDto;
import com.easyterview.wingterview.interview.dto.response.QuestionCreationResponseDto;
import com.easyterview.wingterview.interview.entity.*;
import com.easyterview.wingterview.interview.repository.*;
import com.easyterview.wingterview.rabbitmq.service.RabbitMqService;
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

// 질문 생성 및 선택 (메인/꼬리질문)
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService{

    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final UserRepository userRepository;
    private final QuestionOptionsRepository questionOptionsRepository;
    private final QuestionHistoryRepository questionHistoryRepository;
    private final ReceivedQuestionRepository receivedQuestionRepository;
    private final MainQuestionRepository mainQuestionRepository;
    private final InterviewHistoryRepository interviewHistoryRepository;
    private final InterviewSegmentRepository interviewSegmentRepository;
    private final RabbitMqService rabbitMqService;

    @Override
    @Transactional
    public Object makeQuestion(String interviewId, QuestionCreationRequestDto dto) {

        // 기본 정보 가져오기
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
                            .selectedQuestion(questionHistoryRepository.findByInterview(interview).orElseThrow(QuestionOptionNotFoundException::new).getSelectedQuestion())
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
                            .selectedQuestion(questionHistoryRepository.findByInterview(interview).orElseThrow(QuestionOptionNotFoundException::new).getSelectedQuestion())
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
                        .selectedQuestion(questionHistoryRepository.findByInterview(interview).orElseThrow(QuestionOptionNotFoundException::new).getSelectedQuestion())
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
        }
        // 2. 원래 있었다면 questionIdx + 1로 설정
        else {
            QuestionHistoryEntity oldQuestionHistory = oldQuestionHistoryOpt.get();
            oldQuestionHistory.setSelectedQuestionIdx(oldQuestionHistory.getSelectedQuestionIdx() + 1);
            oldQuestionHistory.setSelectedQuestion(selectedQuestion);
            questionHistoryRepository.save(oldQuestionHistory);
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
}
