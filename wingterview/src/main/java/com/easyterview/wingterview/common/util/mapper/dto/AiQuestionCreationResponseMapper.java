package com.easyterview.wingterview.common.util.mapper.dto;

import com.easyterview.wingterview.interview.dto.response.AiQuestionCreationResponseDto;

public class AiQuestionCreationResponseMapper {
    public static AiQuestionCreationResponseDto of(String question){
        return AiQuestionCreationResponseDto.builder().question(question).build();
    }
}
