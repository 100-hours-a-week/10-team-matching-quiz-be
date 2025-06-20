package com.easyterview.wingterview.quiz.service;

import com.easyterview.wingterview.global.exception.InvalidTokenException;
import com.easyterview.wingterview.global.exception.QuizNotFoundException;
import com.easyterview.wingterview.interview.entity.ReceivedQuestionEntity;
import com.easyterview.wingterview.interview.repository.ReceivedQuestionRepository;
import com.easyterview.wingterview.quiz.dto.request.QuizCreationRequestDto;
import com.easyterview.wingterview.quiz.dto.request.QuizResultItem;
import com.easyterview.wingterview.quiz.dto.request.TodayQuizResultRequestDto;
import com.easyterview.wingterview.quiz.dto.response.*;
import com.easyterview.wingterview.quiz.entity.QuizEntity;
import com.easyterview.wingterview.quiz.entity.QuizSelectionEntity;
import com.easyterview.wingterview.quiz.entity.TodayQuizEntity;
import com.easyterview.wingterview.quiz.repository.QuizRepository;
import com.easyterview.wingterview.quiz.repository.QuizRepositoryCustom;
import com.easyterview.wingterview.quiz.repository.QuizSelectionRepository;
import com.easyterview.wingterview.quiz.repository.TodayQuizRepository;
import com.easyterview.wingterview.rabbitmq.consumer.QuizConsumer;
import com.easyterview.wingterview.rabbitmq.service.RabbitMqService;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
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
    public void sendTodayQuizResult(String userId, TodayQuizResultRequestDto request) {
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
}
