package com.easyterview.wingterview.common.util;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
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

    public static Integer getTime(Timestamp startAt, Timestamp when){
        Instant start = startAt.toInstant();
        Instant end = when.toInstant();
        long seconds = ChronoUnit.SECONDS.between(start, end);

        return (int) seconds;
    }

    public static Timestamp getEndAt(Timestamp originalEndAt){
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        return now.after(originalEndAt) ? originalEndAt : now;
    }
}
