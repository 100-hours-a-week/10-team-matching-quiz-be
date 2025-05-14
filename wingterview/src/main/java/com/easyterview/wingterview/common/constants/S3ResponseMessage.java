package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum S3ResponseMessage implements ResponseMessage{

    URL_FETCH_DONE(200, "presigned url 받아오기 완료"),
    URL_SAVE_DONE(200, "url 저장 완료");

    private final int statusCode;
    private final String message;
}
