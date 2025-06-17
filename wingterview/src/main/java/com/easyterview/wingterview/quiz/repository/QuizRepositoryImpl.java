package com.easyterview.wingterview.quiz.repository;

import com.easyterview.wingterview.quiz.dto.response.QuizInfo;
import com.easyterview.wingterview.quiz.dto.response.QuizListResponse;
import com.easyterview.wingterview.quiz.entity.QQuizEntity;
import com.easyterview.wingterview.quiz.entity.QuizEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class QuizRepositoryImpl implements QuizRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public QuizListResponse findByCursorWithLimit(UUID userId, Boolean wrong, UUID cursor, int limit) {
        QQuizEntity q = QQuizEntity.quizEntity;

        // base 조건 구성
        BooleanBuilder baseCondition = new BooleanBuilder();

        // user id
        baseCondition.and(q.user.id.eq(userId));

        // wrong t/f 여부
        if (Boolean.TRUE.equals(wrong)) {
            baseCondition.and(q.isCorrect.eq(false));
        }

        // createdAt기준 커서보다 최신 갯수( == index) 계산
        Long prevCount = 0L;
        if (cursor != null) {

            // 이게 baseConditiond에 곧바로 and연산을 쳐 해버리면 문제가 생기네 ㅅㅂ
            // deep copy해서 해야함
            BooleanBuilder countCondition = new BooleanBuilder(baseCondition);
            countCondition.and(q.id.gt(cursor));

            prevCount = queryFactory
                    .select(q.count())
                    .from(q)
                    .where(countCondition)
                    .fetchOne();
        }


        // 3. where 조건에 커서 이전만 포함
        BooleanBuilder where = new BooleanBuilder(baseCondition);
        if (cursor != null) {
            where.and(q.id.loe(cursor));
        }


        List<QuizEntity> result = queryFactory
                .selectFrom(q)
                .where(where)
                .orderBy(q.id.desc())  // 시간순 정렬
                .limit(limit + 1)
                .fetch();


        boolean hasNext = result.size() > limit;
        List<QuizEntity> content = hasNext ? result.subList(0, limit) : result;
        UUID nextCursor = hasNext ? result.getLast().getId() : null;

        // 5. questionIdx 부여
        List<QuizInfo> quizInfoList = new ArrayList<>();
        if(prevCount != null) {
            for (int i = 0; i < content.size(); i++) {
                int questionIdx = (int) (prevCount + i + 1);
                quizInfoList.add(QuizInfo.fromEntity(content.get(i), questionIdx));
            }
        }

        return QuizListResponse.builder()
                .quizzes(quizInfoList)
                .hasNext(hasNext)
                .nextCursor(String.valueOf(nextCursor))
                .build();
    }
}
