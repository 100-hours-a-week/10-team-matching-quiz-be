package com.easyterview.wingterview.rabbitmq.consumer;

import com.easyterview.wingterview.quiz.dto.response.FollowupResponse;
import com.easyterview.wingterview.quiz.dto.response.QuizCreationResponseDto;
import com.easyterview.wingterview.quiz.dto.response.QuizItem;
import com.easyterview.wingterview.quiz.entity.QuizEntity;
import com.easyterview.wingterview.quiz.entity.QuizSelectionEntity;
import com.easyterview.wingterview.quiz.entity.TodayQuizEntity;
import com.easyterview.wingterview.quiz.repository.QuizRepository;
import com.easyterview.wingterview.quiz.repository.QuizSelectionRepository;
import com.easyterview.wingterview.quiz.repository.TodayQuizRepository;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizConsumer {

    private final TodayQuizRepository todayQuizRepository;
    private final QuizSelectionRepository quizSelectionRepository;
    private final UserRepository userRepository;

    public void consumeQuiz(FollowupResponse response) {
        log.info("📩 퀴즈 응답 수신: {}", response);

        UUID userId = UUID.fromString(response.getData().getInterviewId());
        UserEntity user = userRepository.findById(userId).orElseThrow();

        QuizCreationResponseDto responseBody = response.getData();
        for (QuizItem item : responseBody.getQuestions()) {

            TodayQuizEntity quiz = TodayQuizEntity.builder()
                    .user(user)
                    .question(item.getQuestion())
                    .correctAnswerIdx(item.getAnswerIndex())
                    .commentary(item.getExplanation())
                    .difficulty(item.getDifficulty())
                    .build();

            todayQuizRepository.save(quiz); // 먼저 저장하고 ID 생성


            List<QuizSelectionEntity> selections = item.getOptions().stream()
                    .map(option -> QuizSelectionEntity.builder()
                            .selection(option)
                            .todayQuiz(quiz)
                            .build())
                    .toList();

            quizSelectionRepository.saveAll(selections);

            quiz.getQuizSelectionEntityList().addAll(selections);
            }


        log.info("✅ 퀴즈 저장 완료 ({}개)", responseBody.getQuestions().size());
    }
}
