package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserResponseMessage implements ResponseMessage{
    USER_INFO_SEND_DONE(200, "유저 정보 전송 완료")

    ;


    private final int statusCode;
    private final String message;
}
