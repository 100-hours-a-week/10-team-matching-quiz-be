package com.easyterview.wingterview.common.util.mapper.dto;

import com.easyterview.wingterview.interview.dto.response.QuestionCreationResponseDto;

import java.util.List;

public class QuestionCreationResponseMapper {
    public static QuestionCreationResponseDto of(List<String> questions){
        return QuestionCreationResponseDto.builder().questions(questions).build();
    }
}
