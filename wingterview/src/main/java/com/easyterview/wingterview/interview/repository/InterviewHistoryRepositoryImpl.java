package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.global.exception.InterviewNotFoundException;
import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.interview.entity.QInterviewHistoryEntity;
import com.easyterview.wingterview.user.dto.response.InterviewDetailDto;
import com.easyterview.wingterview.user.dto.response.InterviewHistoryDto;
import com.easyterview.wingterview.user.dto.response.InterviewItem;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class InterviewHistoryRepositoryImpl implements InterviewHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public InterviewHistoryDto findByCursorWithLimit(String userId, UUID cursor, Integer limit) {
        QInterviewHistoryEntity q = QInterviewHistoryEntity.interviewHistoryEntity;

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(q.user.id.eq(UUID.fromString(userId)));
        if (cursor != null) {
            Timestamp cursorTime = queryFactory
                    .select(q.createdAt)
                    .from(q)
                    .where(q.id.eq(cursor))
                    .fetchOne();

            if (cursorTime == null) throw new InterviewNotFoundException();
            condition.and(q.createdAt.loe(cursorTime));
        }

        List<InterviewHistoryEntity> historyEntityList = queryFactory
                .selectFrom(q)
                .where(condition)
                .orderBy(q.createdAt.desc())
                .limit(limit + 1)
                .fetch();

        List<InterviewItem> interviewItemList = historyEntityList.stream()
                .limit(limit) // 실제 반환 개수 제한
                .map(h -> {
                    long durationSec = 0L;
                    if (h.getCreatedAt() != null && h.getEndAt() != null) {
                        durationSec = (h.getEndAt().getTime() - h.getCreatedAt().getTime()) / 1000;
                    }

                    return InterviewItem.builder()
                            .id(h.getId().toString())
                            .questionCount(h.getSegments().size())
                            .duration(durationSec)
                            .firstQuestion(h.getSegments().getFirst().getSelectedQuestion())
                            .createdAt(h.getCreatedAt())
                            .hasFeedback(h.getSegments().getFirst().getFeedback() != null)
                            .isFeedbackRequested(h.getIsFeedbackRequested())
                            .build();
                })
                .toList();

        boolean hasNext = historyEntityList.size() == limit + 1;
        UUID nextCursor = hasNext ? historyEntityList.getLast().getId() : null;
        return InterviewHistoryDto.builder()
                .history(interviewItemList)
                .hasNext(hasNext)
                .nextCursor(nextCursor != null ? nextCursor.toString() : null)
                .build();
    }
}
