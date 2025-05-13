package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionMessage implements ResponseMessage{
    INVALID_INPUT(400, "INVALID_INPUT"),
    INVALID_TOKEN(400, "INVALID_TOKEN"),
    ALREADY_ENQUEUED(409, "ALREADY_ENQUEUED"),
    QUEUE_CLOSED(410, "QUEUE_CLOSED"),
    INVALID_USER(400, "INVALID_USER"),
    ALREADY_BLOCKED_SEAT(409, "ALREADY_BLOCKED_SEAT"),
    INVALID_UUID(400, "INVALID_UUID"),
    INTERVIEW_NOT_FOUND(404, "INTERVIEW_NOT_FOUND"),
    QUESTION_NOT_FOUND(404, "QUESTION_NOT_FOUND"),
    USER_NOT_FOUND(404, "USER_NOT_FOUND");

    private final int statusCode;
    private final String message;
}
