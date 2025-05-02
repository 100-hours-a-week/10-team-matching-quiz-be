package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserResponseMessage implements ResponseMessage{
    USER_INFO_FETCH_DONE(200, "유저 정보 조회 완료"),
    SEAT_FETCH_DONE(200, "자리 정보 조회 완료"),
    SEAT_CHECK_DONE(200, "자리 상태 조회 완료"),
    USER_INFO_SAVE_DONE(200, "유저 정보 저장 완료");


    private final int statusCode;
    private final String message;
}
