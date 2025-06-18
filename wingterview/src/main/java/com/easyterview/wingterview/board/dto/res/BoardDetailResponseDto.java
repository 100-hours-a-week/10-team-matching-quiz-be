package com.easyterview.wingterview.board.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class BoardDetailResponseDto {
    private final String authorNickname;
    private final String authorProfileImageUrl;
    private final String question;
    private final String modelAnswer;
    private final String feedback;
    private final String authorComment;
    private final Integer viewCnt;
    private final Timestamp createdAt;
}
