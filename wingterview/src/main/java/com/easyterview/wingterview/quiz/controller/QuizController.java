package com.easyterview.wingterview.quiz.controller;

import com.easyterview.wingterview.common.constants.QuizResponseMessage;
import com.easyterview.wingterview.global.response.BaseResponse;
import com.easyterview.wingterview.quiz.dto.response.QuizListResponse;
import com.easyterview.wingterview.quiz.dto.response.QuizStatsResponse;
import com.easyterview.wingterview.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/user/{userId}/quiz-stats")
    public ResponseEntity<BaseResponse> getQuizStats(@PathVariable String userId){
        QuizStatsResponse quizStatsResponse = quizService.getQuizStats(userId);
        return BaseResponse.response(QuizResponseMessage.QUIZ_STAT_FETCH_DONE,quizStatsResponse);
    }

    @GetMapping("/user/{userId}/quizzes")
    public ResponseEntity<BaseResponse> getQuizList(@PathVariable String userId,
                                                    @RequestParam(required = false) Boolean wrong,
                                                    @RequestParam(required = false) String cursor,
                                                    @RequestParam(defaultValue = "5") Integer limit ){
        System.out.println("*************** Controller **************");
        System.out.println(userId);
        System.out.println(wrong);
        System.out.println(cursor);
        System.out.println(limit);

        QuizListResponse quizListResponse = quizService.getQuizList(userId, wrong, cursor, limit);
        return BaseResponse.response(QuizResponseMessage.QUIZ_LIST_FETCH_DONE,quizListResponse);
    }
}
