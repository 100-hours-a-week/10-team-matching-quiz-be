package com.easyterview.wingterview.board.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class BoardItem {
    private final String authorNickname;
    private final String authorProfileImageUrl;
    private final String boardId;
    private final String question;
    private final Integer viewCnt;
    private final Timestamp createdAt;
    private final Boolean isMyPost;
}
