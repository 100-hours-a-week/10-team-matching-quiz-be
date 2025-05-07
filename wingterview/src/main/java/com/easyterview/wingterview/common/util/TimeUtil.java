package com.easyterview.wingterview.common.util;

import com.easyterview.wingterview.interview.enums.Phase;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
public class TimeUtil {

    public static Integer getRemainTime(Timestamp phaseAt, InterviewStatus status) {
        long totalSeconds;

        switch (status.getPhase()) {
            case PROGRESS -> totalSeconds = 20 * 60;
            case FEEDBACK -> totalSeconds = 5 * 60;
            default -> throw new IllegalArgumentException("지원하지 않는 인터뷰 상태입니다.");
        }

        Instant now = Instant.now();
        Instant phaseStart = phaseAt.toInstant();
        long elapsedSeconds = ChronoUnit.SECONDS.between(phaseStart, now);
        log.info("***************  {} ", totalSeconds);
        log.info("**************** {} ",elapsedSeconds);

        long remainSeconds = Math.max(0, totalSeconds - elapsedSeconds);

        return (int) remainSeconds;
    }
}
