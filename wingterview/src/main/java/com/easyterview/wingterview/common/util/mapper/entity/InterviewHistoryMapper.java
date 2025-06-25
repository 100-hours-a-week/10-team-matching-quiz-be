package com.easyterview.wingterview.common.util.mapper.entity;

import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.user.entity.UserEntity;

public class InterviewHistoryMapper {
    public static InterviewHistoryEntity toEntity(UserEntity user){
        return InterviewHistoryEntity.builder()
                .user(user)
                .build();
    }

}
