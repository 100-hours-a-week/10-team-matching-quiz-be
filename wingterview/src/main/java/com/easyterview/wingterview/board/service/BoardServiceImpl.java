package com.easyterview.wingterview.board.service;

import com.easyterview.wingterview.board.dto.req.BoardCreationRequestDto;
import com.easyterview.wingterview.board.dto.res.BoardCreationResponseDto;
import com.easyterview.wingterview.board.dto.res.BoardDetailResponseDto;
import com.easyterview.wingterview.board.dto.res.BoardListResponseDto;
import com.easyterview.wingterview.board.dto.res.Feedback;
import com.easyterview.wingterview.board.entity.BoardEntity;
import com.easyterview.wingterview.board.repository.BoardRepository;
import com.easyterview.wingterview.board.repository.BoardRepositoryCustom;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.BoardNotFoundException;
import com.easyterview.wingterview.global.exception.FeedbackNotReadyException;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.global.exception.UserNotFoundException;
import com.easyterview.wingterview.interview.entity.InterviewFeedbackEntity;
import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.interview.entity.InterviewSegmentEntity;
import com.easyterview.wingterview.interview.repository.InterviewFeedbackRepository;
import com.easyterview.wingterview.interview.repository.InterviewHistoryRepository;
import com.easyterview.wingterview.interview.repository.InterviewSegmentRepository;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardRepositoryCustom boardRepositoryCustom;
    private final InterviewSegmentRepository interviewSegmentRepository;
    private final InterviewFeedbackRepository interviewFeedbackRepository;
    private final InterviewHistoryRepository interviewHistoryRepository;

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
    @Transactional
    public BoardListResponseDto getBoardList(String orderBy, String cursor, Integer limit) {
        return boardRepositoryCustom.findByOrderByAndCursorAndLimit(orderBy, cursor == null ? null : UUID.fromString(cursor), limit);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDetailResponseDto getBoardDetail(String boardId) {
        BoardEntity board = boardRepository.findById(UUID.fromString(boardId)).orElseThrow(BoardNotFoundException::new);

        if (board.getInterviewSegment().getFeedback() == null)
            throw new FeedbackNotReadyException();

        InterviewFeedbackEntity feedback = board.getInterviewSegment().getFeedback();

        return BoardDetailResponseDto.builder()
                .viewCnt(board.getViewCnt())
                .authorComment(board.getComment())
                .authorNickname(board.getUser().getNickname())
                .authorProfileImageUrl(board.getUser().getProfileImageUrl())
                .feedback(Feedback.builder()
                        .score(feedback.getScore())
                        .details(feedback.getDetails())
                        .improvements(feedback.getImprovements())
                        .goodPoints(feedback.getGoodPoints())
                        .build())
                .modelAnswer(board.getInterviewSegment().getFeedback().getCorrectAnswer())
                .createdAt(board.getCreatedAt())
                .question(board.getInterviewSegment().getSelectedQuestion())
                .build();
    }

    @Override
    @Transactional
    public void createDummyBoards(int count) {
        // 1. 유저 미리 조회
        UserEntity user = userRepository.findByName("김광현")
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: 김광현"));



        // 3. Batch insert
        for (int i = 1; i <= count; i++) {
            InterviewHistoryEntity interviewHistory = InterviewHistoryEntity.builder()
                    .user(user)
                    .createdAt(Timestamp.valueOf(LocalDateTime.now().minusSeconds(10)))
                    .endAt(Timestamp.valueOf(LocalDateTime.now()))
                    .isFeedbackRequested(true)
                    .build();

            interviewHistoryRepository.save(interviewHistory);

            InterviewSegmentEntity interviewSegment = InterviewSegmentEntity.builder()
                    .segmentOrder(1)
                    .fromTime(0)
                    .toTime(10)
                    .selectedQuestion("선택된질문질문")
                    .interviewHistory(interviewHistory)
                    .build();

            interviewSegmentRepository.save(interviewSegment);
            interviewHistory.getSegments().add(interviewSegment);

            InterviewFeedbackEntity dummyFeedback = InterviewFeedbackEntity.builder()
                    .correctAnswer("정답")
                    .score(5)
                    .improvements("개선할 점")
                    .interviewSegment(interviewSegment)
                    .details("디테일")
                    .goodPoints("굿 포인트")
                    .build();

            interviewFeedbackRepository.save(dummyFeedback);
            interviewSegment.setFeedback(dummyFeedback);

            boardRepository.save(BoardEntity.builder()
                    .comment("comment" + i)
                    .user(user)
                    .interviewSegment(interviewSegment)
                    .build());
        }
    }
}
