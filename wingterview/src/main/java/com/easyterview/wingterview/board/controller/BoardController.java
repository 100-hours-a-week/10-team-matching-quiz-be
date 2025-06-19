package com.easyterview.wingterview.board.controller;

import com.easyterview.wingterview.board.dto.req.BoardCreationRequestDto;
import com.easyterview.wingterview.board.dto.res.BoardCreationResponseDto;
import com.easyterview.wingterview.board.dto.res.BoardDetailResponseDto;
import com.easyterview.wingterview.board.dto.res.BoardListResponseDto;
import com.easyterview.wingterview.board.service.BoardService;
import com.easyterview.wingterview.common.constants.BoardResponseMessage;
import com.easyterview.wingterview.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/board/{segmentId}")
    public ResponseEntity<BaseResponse> createBoard(@RequestBody BoardCreationRequestDto requestDto, @PathVariable String segmentId){
        BoardCreationResponseDto response = boardService.createBoard(requestDto, segmentId);
        return BaseResponse.response(BoardResponseMessage.BOARD_CREATION_DONE,response);
    }

    @GetMapping("/board")
    public ResponseEntity<BaseResponse> getBoardList(@RequestParam String orderBy,
                                                     @RequestParam(required = false) String cursor,
                                                     @RequestParam(defaultValue = "10") Integer limit){
        log.info("**********보드 리스트 가져오기**********");
        BoardListResponseDto response = boardService.getBoardList(orderBy,cursor,limit);
        return BaseResponse.response(BoardResponseMessage.BOARD_LIST_FETCH_DONE, response);
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<BaseResponse> getBoardDetail(@PathVariable String boardId){
        BoardDetailResponseDto response = boardService.getBoardDetail(boardId);
        return BaseResponse.response(BoardResponseMessage.BOARD_FETCH_DONE,response);
    }
}
