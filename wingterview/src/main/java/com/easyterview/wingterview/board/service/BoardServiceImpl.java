package com.easyterview.wingterview.board.service;

import com.easyterview.wingterview.board.dto.req.BoardCreationRequestDto;
import com.easyterview.wingterview.board.dto.res.BoardCreationResponseDto;
import com.easyterview.wingterview.board.dto.res.BoardDetailResponseDto;
import com.easyterview.wingterview.board.dto.res.BoardListResponseDto;
import com.easyterview.wingterview.board.entity.BoardEntity;
import com.easyterview.wingterview.board.repository.BoardRepository;
import com.easyterview.wingterview.board.repository.BoardRepositoryCustom;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.BoardNotFoundException;
import com.easyterview.wingterview.global.exception.FeedbackNotReadyException;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.global.exception.UserNotFoundException;
import com.easyterview.wingterview.interview.entity.InterviewSegmentEntity;
import com.easyterview.wingterview.interview.repository.InterviewSegmentRepository;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BoardServiceImpl implements BoardService{

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardRepositoryCustom boardRepositoryCustom;
    private final InterviewSegmentRepository interviewSegmentRepository;

    @Override
    @Transactional
    public BoardCreationResponseDto createBoard(BoardCreationRequestDto requestDto, String segmentId) {
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken()).orElseThrow(UserNotFoundException::new);

        UUID boardId = boardRepository.save(BoardEntity.builder()
                .comment(requestDto.getComment())
                .user(user)
                .interviewSegment(interviewSegmentRepository.findById(UUID.fromString(segmentId)).orElseThrow(InterviewNotFoundException::new))
                .build()).getId();

        return BoardCreationResponseDto.builder()
                .boardId(boardId.toString())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BoardListResponseDto getBoardList(String orderBy, String cursor, Integer limit) {
        return boardRepositoryCustom.findByOrderByAndCursorAndLimit(orderBy, UUID.fromString(cursor),limit);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDetailResponseDto getBoardDetail(String boardId) {
        BoardEntity board = boardRepository.findById(UUID.fromString(boardId)).orElseThrow(BoardNotFoundException::new);

        if(board.getInterviewSegment().getFeedback() == null)
            throw new FeedbackNotReadyException();

        return BoardDetailResponseDto.builder()
                .viewCnt(board.getViewCnt())
                .authorComment(board.getComment())
                .authorNickname(board.getUser().getNickname())
                .authorProfileImageUrl(board.getUser().getProfileImageUrl())
                .feedback(board.getInterviewSegment().getFeedback().getCommentary())
                .modelAnswer(board.getInterviewSegment().getFeedback().getCorrectAnswer())
                .createdAt(board.getCreatedAt())
                .question(board.getInterviewSegment().getSelectedQuestion())
                .build();
    }
}
