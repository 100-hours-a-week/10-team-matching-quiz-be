package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionMessage implements ResponseMessage{
    INVALID_INPUT(400, "유효하지 않은 입력"),
    INVALID_TOKEN(400, "유효하지 않은 토큰"),
    ALREADY_ENQUEUED(409, "이미 진입한 사용자"),
    QUEUE_CLOSED(410, "이미 매칭 큐가 닫힘"),
    INVALID_USER(400, "매칭 큐에 진입하지 않은 사용자"),
    ALREADY_BLOCKED_SEAT(409, "이미 막힌 자리"),
    INVALID_UUID(400, "잘못된 UUID 형식"),
    INTERVIEW_NOT_FOUND(404, "존재하지 않는 인터뷰"),
    QUESTION_NOT_FOUND(404, "존재하지 않는 질문 목록");

    private final int statusCode;
    private final String message;
}
