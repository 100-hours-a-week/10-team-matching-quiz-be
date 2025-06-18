package com.easyterview.wingterview.board.service;

import com.easyterview.wingterview.board.dto.req.BoardCreationRequestDto;
import com.easyterview.wingterview.board.dto.res.BoardCreationResponseDto;
import com.easyterview.wingterview.board.dto.res.BoardDetailResponseDto;
import com.easyterview.wingterview.board.dto.res.BoardListResponseDto;

public interface BoardService {
    BoardCreationResponseDto createBoard(BoardCreationRequestDto requestDto, String segmentId);

    BoardListResponseDto getBoardList(String orderBy, String cursor, Integer limit);

    BoardDetailResponseDto getBoardDetail(String boardId);
}
