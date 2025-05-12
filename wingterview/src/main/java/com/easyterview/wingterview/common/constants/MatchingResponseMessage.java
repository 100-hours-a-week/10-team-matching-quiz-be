package com.easyterview.wingterview.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatchingResponseMessage implements ResponseMessage{

    ENQUEUE_DONE(200, "매칭 큐 진입 완료"),
    MATCHING_RESULT_FETCH_DONE(200, "매칭 정보 받아오기 완료"),
    MATCHING_PENDING(200, "매칭 대기중"),
    MATCHING_STATISTICS_FETCH_DONE(200, "매칭 통계 받아오기 완료"),
    MATCHING_DONE(201, "매칭 알고리즘 시행 완료");

    private final int statusCode;
    private final String message;
}
