package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthResponseMessage implements ResponseMessage{
    AUTHORIZATION_CODE_SEND_DONE(200, "Authorization code send done"),
    LOGIN_SUCCESS(200, "로그인 성공"),
    INVALID_TOKEN(401, "토큰이 유효하지 않음"),
    EXPIRED_TOKEN(401, "토큰이 만료됨"),

    LOGIN_FAILED(400, "로그인 실패"),
    MISSING_AUTH_CODE(400, "auth code 없음"),
    WRONG_PROVIDER(400, "잘못된 provider"),
    ;

    private final int statusCode;
    private final String message;
}
