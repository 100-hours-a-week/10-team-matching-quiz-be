package com.easyterview.wingterview.common.util.mapper.entity;

import com.easyterview.wingterview.interview.entity.InterviewEntity;

public class InterviewMapper {
    public static InterviewEntity toEntity(boolean isAiInterview){
        return InterviewEntity.builder()
                .isAiInterview(isAiInterview)
                .build();
    }
}
