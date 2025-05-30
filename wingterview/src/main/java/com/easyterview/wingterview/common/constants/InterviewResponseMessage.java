package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterviewResponseMessage implements ResponseMessage {

    INTERVIEW_PHASE_UPDATED(200, "다음 분기 처리 완료"),
    INTERVIEW_PHASE_FETCH_DONE(200, "면접 상태 받아오기 완료"),
    QUESTION_FETCH_DONE(200, "질문 생성 완료"),
    QUESTION_SELECT_DONE(200, "질문 선택 완료"),
    FEEDBACK_SEND_DONE(200, "피드백 전송 완료"),
    AI_INTERVIEW_CREATED(200, "AI 인터뷰 시작 완료"),
    INTERVIEW_TIME_INITIALIZED(200, "AI 인터뷰 시간 초기화 완료"),
    INTERVIEW_DELETE_DONE(200, "인터뷰 종료 완료");

    private final int statusCode;
    private final String message;
}
