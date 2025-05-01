package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionMessage implements ResponseMessage{
    NOT_VALID_INPUT(400, "유효하지 않은 입력");



    private final int statusCode;
    private final String message;
}
