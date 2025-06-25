package com.easyterview.wingterview.common.util.mapper.entity;

import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.InterviewTimeEntity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class InterviewTimeMapper {
    public static InterviewTimeEntity toEntity(Integer timeMinutes, InterviewEntity interview){
        return InterviewTimeEntity.builder()
                .endAt(Timestamp.valueOf(LocalDateTime.now().plusMinutes(timeMinutes)))
                .interview(interview)
                .build();
    }
}
