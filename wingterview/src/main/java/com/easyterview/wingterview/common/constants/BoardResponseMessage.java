package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardResponseMessage implements ResponseMessage{
    BOARD_CREATION_DONE(200, "게시판 생성 완료"),
    BOARD_FETCH_DONE(200, "게시글 전송 완료"),
    BOARD_LIST_FETCH_DONE(200, "게시글 리스트 전송 완료");
    private final int statusCode;
    private final String message;
}
