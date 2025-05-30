package com.easyterview.wingterview.common.util;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
public class TimeUtil {

    public static Integer getRemainTime(Timestamp endAt) {
        Instant now = Instant.now();
        Instant end = endAt.toInstant();
        long remainSeconds = ChronoUnit.SECONDS.between(now, end);
        log.info("**************** remain: {} seconds", remainSeconds);

        return (int) Math.max(0, remainSeconds);
    }
}
