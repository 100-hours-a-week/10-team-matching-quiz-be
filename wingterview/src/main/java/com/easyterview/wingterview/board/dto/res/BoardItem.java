package com.easyterview.wingterview.board.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardItem {
    private String authorNickname;
    private String authorProfileImageUrl;
    private String boardId;
    private String question;
    private Integer viewCnt;
    private Timestamp createdAt;
    private Boolean isMyPost;
}
