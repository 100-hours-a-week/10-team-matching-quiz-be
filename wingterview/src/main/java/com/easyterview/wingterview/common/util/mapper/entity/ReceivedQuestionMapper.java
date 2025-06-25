package com.easyterview.wingterview.common.util.mapper.entity;

import com.easyterview.wingterview.interview.entity.ReceivedQuestionEntity;
import com.easyterview.wingterview.user.entity.UserEntity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ReceivedQuestionMapper {
    public static ReceivedQuestionEntity toEntity(String question, UserEntity user){
        return
                ReceivedQuestionEntity.builder()
                        .contents(question)
                        .receivedAt(Timestamp.valueOf(LocalDateTime.now()))
                        .user(user)
                        .build();
    }
}
