package com.easyterview.wingterview.board.repository;

import com.easyterview.wingterview.board.dto.res.BoardListResponseDto;

import java.util.UUID;

public interface BoardRepositoryCustom {
    BoardListResponseDto findByOrderByAndCursorAndLimit(String orderBy, UUID cursor, Integer limit);
}
