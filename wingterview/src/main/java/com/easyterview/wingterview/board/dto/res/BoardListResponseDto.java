package com.easyterview.wingterview.board.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor // ✅ Jackson 역직렬화용 기본 생성자
@AllArgsConstructor
public class BoardListResponseDto {
    private List<BoardItem> boardList;
    private Boolean hasNext;
    private String nextCursor;
}
