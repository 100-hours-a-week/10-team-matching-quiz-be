package com.easyterview.wingterview.quiz.controller;

import com.easyterview.wingterview.global.response.BaseResponse;
import com.easyterview.wingterview.quiz.dto.response.QuizStatsResponse;
import com.easyterview.wingterview.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/{userId}/quiz-stats")
    public ResponseEntity<BaseResponse> getQuizStats(@PathVariable String userId){
        QuizStatsResponse quizStatsResponse = quizService.getQuizStats(userId);
    }
}
