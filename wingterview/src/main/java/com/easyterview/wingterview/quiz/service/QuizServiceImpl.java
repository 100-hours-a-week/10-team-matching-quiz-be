package com.easyterview.wingterview.quiz.service;

import com.easyterview.wingterview.global.exception.InvalidTokenException;
import com.easyterview.wingterview.global.exception.QuizNotFoundException;
import com.easyterview.wingterview.global.exception.UserNotFoundException;
import com.easyterview.wingterview.quiz.entity.CsQuizSelectionEntity;
import com.easyterview.wingterview.interview.entity.ReceivedQuestionEntity;
import com.easyterview.wingterview.interview.repository.ReceivedQuestionRepository;
import com.easyterview.wingterview.quiz.dto.request.QuizCreationRequestDto;
import com.easyterview.wingterview.quiz.dto.request.QuizResultItem;
import com.easyterview.wingterview.quiz.dto.request.QuizResultRequestDto;
import com.easyterview.wingterview.quiz.dto.response.*;
import com.easyterview.wingterview.quiz.entity.*;
import com.easyterview.wingterview.quiz.enums.QuizCategory;
import com.easyterview.wingterview.quiz.repository.*;
import com.easyterview.wingterview.rabbitmq.consumer.QuizConsumer;
import com.easyterview.wingterview.rabbitmq.service.RabbitMqService;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService{

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizRepositoryCustom quizRepositoryCustom;
    private final TodayQuizRepository todayQuizRepository;
    private final QuizSelectionRepository quizSelectionRepository;
    private final ReceivedQuestionRepository receivedQuestionRepository;
    private final RabbitMqService rabbitMqService;
    private final QuizConsumer quizConsumer;
    private final CsQuizRepository csQuizRepository;
    private final UserCsQuizRepository userCsQuizRepository;

    @Override
    public QuizStatsResponse getQuizStats(String userId) {
        List<QuizEntity> quiz = quizRepository.findAllByUserId(UUID.fromString(userId));
        int correctQuizCnt = quiz.stream().filter(QuizEntity::getIsCorrect).toList().size();
        float correctRate = 0.0f;
        if(!quiz.isEmpty()){
            correctRate = (float) correctQuizCnt/quiz.size();
            correctRate = Math.round(correctRate * 100);
        }
        return QuizStatsResponse.builder()
                .correctRate(correctRate)
                .build();
    }

    @Override
    public QuizListResponse getQuizList(String userId, Boolean wrong, String cursor, Integer limit) {
        return quizRepositoryCustom.findByCursorWithLimit(UUID.fromString(userId),wrong,cursor == null ? null : UUID.fromString(cursor),limit);
    }

    @Override
    public TodayQuizListResponse getTodayQuiz(String userId) {
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(InvalidTokenException::new);

        List<TodayQuizEntity> todayQuizEntityList = todayQuizRepository.findByUser(user);

        if (todayQuizEntityList.isEmpty()) {
            throw new QuizNotFoundException();
        }

        List<TodayQuiz> todayQuizList = todayQuizEntityList.stream().map(e -> {
            List<QuizSelectionEntity> quizSelectionEntityList = quizSelectionRepository.findAllByTodayQuiz(e);
            return
            TodayQuiz.builder()
                    .question(e.getQuestion())
                    .quizIdx(e.getQuestionIdx())
                    .commentary(e.getCommentary())
                    .options(quizSelectionEntityList.stream().map(QuizSelectionEntity::getSelection).toList())
                    .answerIdx(e.getCorrectAnswerIdx())
                    .userAnswer(e.getUserSelection())   // 문제 하나 봤을 때 null이면 안푼거, null 아니면 푼거
                    .difficulty(e.getDifficulty())
                    .build();
        }).toList();

//        todayQuizList.forEach(q -> System.out.println(q.getQuestion()));
//        System.out.println("******8*****");
//        todayQuizList.forEach(q -> System.out.println(q.getAnswerIdx()));
//        System.out.println("**************");
//        todayQuizList.forEach(q -> System.out.println(q.getOptions().get(1)));


        return TodayQuizListResponse.builder()
                .quizList(todayQuizList)
                .build();
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 매일 00:00에 실행
    public void createTodayQuiz() {
        List<UserEntity> userList = userRepository.findAll();
        userList.forEach(user -> {
            List<String> questionHistoryList = receivedQuestionRepository.findTop10ByUserIdOrderByReceivedAt(user.getId()).stream().map(ReceivedQuestionEntity::getContents).toList();
            QuizCreationRequestDto request = QuizCreationRequestDto.builder()
                    .questionHistoryList(questionHistoryList)
                    .userId(user.getId().toString())
                    .build();


            rabbitMqService.sendQuizCreation(request);
            log.info("📤 복습 퀴즈 생성 요청 전송: {}", request);
        });
    }

    @RabbitListener(queues = "quiz.response.queue")
    public void handleQuizResponse(FollowupResponse responseDto) {
        log.info("📥 복습 퀴즈 생성 응답 수신: {}", responseDto);
        quizConsumer.consumeQuiz(responseDto.getData());
    }


    @Override
    @Transactional
    public void sendTodayQuizResult(String userId, QuizResultRequestDto request) {
        List<TodayQuizEntity> todayQuizEntityList = todayQuizRepository.findByUserId(UUID.fromString(userId));
        Map<Integer, TodayQuizEntity> quizMap = todayQuizEntityList.stream()
                .collect(Collectors.toMap(TodayQuizEntity::getQuestionIdx, q -> q));

        List<QuizEntity> solvedQuizzes = new ArrayList<>();
        for (QuizResultItem item : request.getQuizzes()) {
            TodayQuizEntity entity = quizMap.get(item.getQuizIdx());

            if (entity != null) {
                entity.setUserSelection(item.getUserSelection());
                entity.setIsCorrect(item.getIsCorrect());

                solvedQuizzes.add(QuizEntity.builder()
                        .question(entity.getQuestion())
                        .correctAnswer(
                                entity.getQuizSelectionEntityList()
                                        .stream()
                                        .filter(e -> e.getSelectionIdx().equals(entity.getCorrectAnswerIdx()))
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalArgumentException("정답 선택지를 찾을 수 없습니다."))
                                        .getSelection()
                        )
                        .user(entity.getUser())
                        .commentary(entity.getCommentary())
                        .isCorrect(item.getIsCorrect())
                        .solvedAt(Timestamp.valueOf(LocalDateTime.now()))
                        .userAnswer(
                                entity.getQuizSelectionEntityList()
                                        .stream()
                                        .filter(e -> e.getSelectionIdx().equals(item.getUserSelection()))
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalArgumentException("선택지를 찾을 수 없습니다."))
                                        .getSelection()
                        )
                        .build());
            }
        }

        todayQuizRepository.saveAllAndFlush(todayQuizEntityList);
        quizRepository.saveAllAndFlush(solvedQuizzes);
    }

    @Override
    @Transactional(readOnly = true)
    public TodayQuizListResponse getCsQuizList(String userId) {
        List<UserCsQuizEntity> userCsQuizList = userCsQuizRepository.findAllWithChoicesByUserId(UUID.fromString(userId));
        if(userCsQuizList.isEmpty())
            throw new QuizNotFoundException();

        List<TodayQuiz> quizList = userCsQuizList.stream().map(c -> {
            List<CsQuizSelectionEntity> choices = c.getCsQuiz().getChoices();
            return
            TodayQuiz.builder()
                    .quizIdx(c.getQuizIdx())
                    .question(c.getCsQuiz().getQuestion())
                    .options(choices.stream().map(CsQuizSelectionEntity::getContent).toList())
                    .commentary(c.getCsQuiz().getExplanation())
                    .difficulty(null)
                    .answerIdx(choices.stream()
                            .filter(CsQuizSelectionEntity::getIsAnswer)
                            .findFirst()
                            .map(CsQuizSelectionEntity::getOptionIdx)
                            .orElseThrow(() -> new RuntimeException("정답이 없습니다")))
                    .userAnswer(c.getUserAnswerIdx())
                    .build();
        }).toList();

        return TodayQuizListResponse.builder()
                .quizList(quizList)
                .build();
    }

    @Override
    @Transactional
    public void makeCsQuizList(String userId, String category) {
        UserEntity user = userRepository.findById(UUID.fromString(userId)).orElseThrow(UserNotFoundException::new);
        userCsQuizRepository.deleteAllByUserId(UUID.fromString(userId));
        String quizCategory = QuizCategory.fromDisplayName(category).name();
        AtomicInteger quizIdx = new AtomicInteger(1);
        List<UserCsQuizEntity> userCsQuizEntityList = csQuizRepository.findTop10RandomByCategory(quizCategory).stream().map(c ->
            UserCsQuizEntity.builder()
                    .csQuiz(c)
                    .user(user)
                    .quizIdx(quizIdx.getAndIncrement())
                    .build()
        ).toList();

        userCsQuizRepository.saveAll(userCsQuizEntityList);
    }

    @Override
    @Transactional
    public void sendCsQuizResult(String userId, QuizResultRequestDto request) {
        List<UserCsQuizEntity> userCsQuizEntityList = userCsQuizRepository.findAllWithChoicesByUserId(UUID.fromString(userId));
        System.out.println(userCsQuizEntityList.get(0).getCsQuiz().getQuestion());
        System.out.println(userCsQuizEntityList.get(1).getCsQuiz().getQuestion());
        Map<Integer, UserCsQuizEntity> quizMap = userCsQuizEntityList.stream()
                .collect(Collectors.toMap(UserCsQuizEntity::getQuizIdx, q -> q));

        List<QuizEntity> solvedQuizzes = new ArrayList<>();
        for (QuizResultItem item : request.getQuizzes()) {
            UserCsQuizEntity entity = quizMap.get(item.getQuizIdx());

            if (entity != null) {
                entity.setUserAnswerIdx(item.getUserSelection());
                entity.setIsCorrect(item.getIsCorrect());

                solvedQuizzes.add(QuizEntity.builder()
                        .question(entity.getCsQuiz().getQuestion())
                        .correctAnswer(
                                entity.getCsQuiz().getChoices()
                                        .stream()
                                        .filter(CsQuizSelectionEntity::getIsAnswer)
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalArgumentException("정답 선택지를 찾을 수 없습니다."))
                                        .getContent()
                        )
                        .user(entity.getUser())
                        .commentary(entity.getCsQuiz().getExplanation())
                        .isCorrect(item.getIsCorrect())
                        .solvedAt(Timestamp.valueOf(LocalDateTime.now()))
                        .userAnswer(
                                entity.getCsQuiz().getChoices()
                                        .stream()
                                        .filter(e -> e.getOptionIdx().equals(item.getUserSelection()))
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalArgumentException("선택지를 찾을 수 없습니다."))
                                        .getContent()
                        )
                        .build());
            }
        }

        userCsQuizRepository.saveAllAndFlush(userCsQuizEntityList);
        quizRepository.saveAllAndFlush(solvedQuizzes);

        System.out.println(userCsQuizEntityList.get(0).getUserAnswerIdx());
        System.out.println(userCsQuizEntityList.get(1).getUserAnswerIdx());
    }
}
