package com.easyterview.wingterview.board.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BoardListResponseDto {
    private List<BoardItem> boardList;
    private Boolean hasNext;
    private String nextCursor;
}
