package com.easyterview.wingterview.common.util.mapper.entity;

import com.easyterview.wingterview.interview.entity.InterviewEntity;
import com.easyterview.wingterview.interview.entity.QuestionHistoryEntity;

public class QuestionHistoryMapper {
    public static QuestionHistoryEntity toEntity(InterviewEntity interview, String question) {
        return QuestionHistoryEntity.builder()
                .interview(interview)
                .selectedQuestion(question)
                .selectedQuestionIdx(1)
                .build();
    }
}
