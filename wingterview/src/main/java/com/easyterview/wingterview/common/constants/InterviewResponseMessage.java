package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterviewResponseMessage implements ResponseMessage {

    INTERVIEW_PHASE_UPDATED(200, "다음 분기 처리 완료");

    private final int statusCode;
    private final String message;
}
