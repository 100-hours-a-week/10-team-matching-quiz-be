package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuizResponseMessage implements ResponseMessage{

    QUIZ_STAT_FETCH_DONE(200, "퀴즈 정보 전송 완료"),
    QUIZ_LIST_FETCH_DONE(200, "퀴즈 리스트 전송 완료"),
    TODAY_QUIZ_FETCH_DONE(200, "오늘의 퀴즈 전송 완료"),
    QUIZ_RESULT_SEND_DONE(200, "오늘의 퀴즈 결과 저장 완료");

    private final int statusCode;
    private final String message;
}
