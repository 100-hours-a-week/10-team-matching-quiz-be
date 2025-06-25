package com.easyterview.wingterview.common.util.mapper.entity;

import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.QuestionOptionsEntity;

public class QuestionOptionsMapper {
    public static QuestionOptionsEntity toEntity(InterviewEntity interview, String question){
        return QuestionOptionsEntity.builder()
                .interview(interview)
                .option(question)
                .build();
    }
}
