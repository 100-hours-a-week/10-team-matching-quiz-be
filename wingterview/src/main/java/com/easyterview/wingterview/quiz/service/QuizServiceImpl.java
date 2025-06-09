package com.easyterview.wingterview.quiz.service;

import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.InvalidTokenException;
import com.easyterview.wingterview.quiz.dto.response.QuizListResponse;
import com.easyterview.wingterview.quiz.dto.response.QuizStatsResponse;
import com.easyterview.wingterview.quiz.entity.QuizEntity;
import com.easyterview.wingterview.quiz.repository.QuizRepository;
import com.easyterview.wingterview.quiz.repository.QuizRepositoryCustom;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService{

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizRepositoryCustom quizRepositoryCustom;

    @Override
    public QuizStatsResponse getQuizStats(String userId) {
        List<QuizEntity> quiz = quizRepository.findAllByUserId(UUID.fromString(userId));
        int correctQuizCnt = quiz.stream().filter(QuizEntity::getIsCorrect).toList().size();
        float correctRate = 0.0f;
        if(!quiz.isEmpty()){
            correctRate = (float) correctQuizCnt/quiz.size();
            correctRate = Math.round(correctRate * 100) / 100.0f;
        }
        return QuizStatsResponse.builder()
                .correctRate(correctRate)
                .build();
    }

    @Override
    public QuizListResponse getQuizList(String userId, Boolean wrong, String cursor, Integer limit) {
        return quizRepositoryCustom.findByCursorWithLimit(UUID.fromString(userId),wrong,cursor == null ? null : UUID.fromString(cursor),limit);
    }
}
