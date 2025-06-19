package com.easyterview.wingterview.board.repository;

import com.easyterview.wingterview.board.dto.res.BoardItem;
import com.easyterview.wingterview.board.dto.res.BoardListResponseDto;
import com.easyterview.wingterview.board.entity.BoardEntity;
import com.easyterview.wingterview.board.entity.QBoardEntity;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.BoardNotFoundException;
import com.easyterview.wingterview.global.exception.IllegalOrderByStatementException;
import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.interview.entity.QInterviewSegmentEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class BoardRepositoryCustomImpl implements BoardRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public BoardListResponseDto findByOrderByAndCursorAndLimit(String orderBy, UUID cursor, Integer limit) {
        QBoardEntity q = QBoardEntity.boardEntity;
        QInterviewSegmentEntity s = QInterviewSegmentEntity.interviewSegmentEntity;

        BooleanBuilder condition = new BooleanBuilder();
        if (cursor != null) {
            if(orderBy.equals("latest")) {
                Timestamp cursorTime = queryFactory
                        .select(q.createdAt)
                        .from(q)
                        .where(q.id.eq(cursor))
                        .fetchOne();

                if (cursorTime == null) throw new BoardNotFoundException();
                condition.and(q.createdAt.loe(cursorTime));
            }
            else if(orderBy.equals("popular")){
                Integer viewCnt = queryFactory
                        .select(q.viewCnt)
                        .from(q)
                        .where(q.id.eq(cursor))
                        .fetchOne();

                if (viewCnt == null) throw new BoardNotFoundException();
                condition.and(q.viewCnt.loe(viewCnt));
            }
            else{
                throw new IllegalOrderByStatementException();
            }
        }

        List<BoardEntity> boardEntityList = queryFactory
                .selectFrom(q)
                .leftJoin(q.interviewSegment, s).fetchJoin()
                .where(condition)
                .orderBy(orderBy.equals("조회수순") ? q.viewCnt.desc() : q.createdAt.desc())
                .limit(limit+1)
                .fetch();

        List<BoardItem> boardItems = boardEntityList.stream().map(b ->
            BoardItem.builder()
                    .authorNickname(b.getUser().getNickname())
                    .authorProfileImageUrl(b.getUser().getProfileImageUrl())
                    .boardId(b.getId().toString())
                    .question(b.getInterviewSegment().getSelectedQuestion())
                    .isMyPost(b.getUser().getId().equals(UUIDUtil.getUserIdFromToken()))
                    .viewCnt(b.getViewCnt())
                    .createdAt(b.getCreatedAt())
                    .build()
        ).toList();

        boolean hasNext = boardEntityList.size() == limit+1;
        UUID nextCursor = hasNext ? boardEntityList.getLast().getId() : null;
        return BoardListResponseDto.builder()
                .boardList(boardItems)
                .nextCursor(nextCursor != null ? nextCursor.toString() : null)
                .hasNext(hasNext)
                .build();
    }
}
