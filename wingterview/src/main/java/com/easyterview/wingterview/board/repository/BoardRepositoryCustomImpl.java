package com.easyterview.wingterview.board.repository;

import com.easyterview.wingterview.board.dto.res.BoardItem;
import com.easyterview.wingterview.board.dto.res.BoardListResponseDto;
import com.easyterview.wingterview.board.entity.BoardEntity;
import com.easyterview.wingterview.board.entity.QBoardEntity;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.BoardNotFoundException;
import com.easyterview.wingterview.global.exception.IllegalOrderByStatementException;
import com.easyterview.wingterview.interview.entity.QInterviewSegmentEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
@Slf4j
public class BoardRepositoryCustomImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final RedisTemplate<String, Object> redisTemplate;

    private final Duration TTL = Duration.ofMinutes(10);

    @Override
    public BoardListResponseDto findByOrderByAndCursorAndLimit(String orderBy, UUID cursor, Integer limit) {
        String cursorStr = cursor == null ? "null" : cursor.toString();
        String redisKey = String.format("board::list::%s::%s::%d", orderBy, cursorStr, limit);

        // 1. Redis 캐시 체크
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached instanceof BoardListResponseDto dto) {
            log.info("Redis cache hit: {}", redisKey);
            return dto;
        }

        log.info("Redis cache miss: {}", redisKey);

        // 2. DB 조회
        BoardListResponseDto responseDto = queryBoardList(orderBy, cursor, limit);
        if (responseDto != null) {
            redisTemplate.opsForValue().set(redisKey, responseDto, TTL);
            log.info("Redis cache saved: {}", redisKey); // ✅ 이 줄 추가
        }

        return responseDto;
    }

    private BoardListResponseDto queryBoardList(String orderBy, UUID cursor, Integer limit) {
        QBoardEntity q = QBoardEntity.boardEntity;
        QInterviewSegmentEntity s = QInterviewSegmentEntity.interviewSegmentEntity;

        BooleanBuilder condition = new BooleanBuilder();
        if (cursor != null) {
            if (orderBy.equals("latest")) {
                Timestamp cursorTime = queryFactory
                        .select(q.createdAt)
                        .from(q)
                        .where(q.id.eq(cursor))
                        .fetchOne();
                if (cursorTime == null) throw new BoardNotFoundException();
                condition.and(q.createdAt.loe(cursorTime));
            } else if (orderBy.equals("popular")) {
                Integer viewCnt = queryFactory
                        .select(q.viewCnt)
                        .from(q)
                        .where(q.id.eq(cursor))
                        .fetchOne();
                if (viewCnt == null) throw new BoardNotFoundException();
                condition.and(q.viewCnt.loe(viewCnt));
            } else {
                throw new IllegalOrderByStatementException();
            }
        }

        List<BoardEntity> boardEntityList = queryFactory
                .selectFrom(q)
                .leftJoin(q.interviewSegment, s).fetchJoin()
                .where(condition)
                .orderBy(orderBy.equals("popular") ? q.viewCnt.desc() : q.createdAt.desc())
                .limit(limit + 1)
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

        boolean hasNext = boardEntityList.size() == limit + 1;
        UUID nextCursor = hasNext ? boardEntityList.getLast().getId() : null;
        return BoardListResponseDto.builder()
                .boardList(boardItems)
                .nextCursor(nextCursor != null ? nextCursor.toString() : null)
                .hasNext(hasNext)
                .build();
    }
}
