package com.easyterview.wingterview.common.util.mapper.dto;

import com.easyterview.wingterview.interview.dto.request.FollowUpQuestionRequest;
import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.entity.InterviewEntity;

import java.util.List;

public class FollowUpQuestionRequestMapper {
    public static FollowUpQuestionRequest of(InterviewEntity interview, QuestionCreationRequestDto dto, boolean isReMakeQuestion, List<String> passedQuestions){
        return FollowUpQuestionRequest.builder()
                .interviewId(interview.getId().toString())
                .selectedQuestion(dto.getQuestion())
                .keyword(dto.getKeywords())
                .passedQuestions(isReMakeQuestion ? passedQuestions : null)
                .build();
    }
}
