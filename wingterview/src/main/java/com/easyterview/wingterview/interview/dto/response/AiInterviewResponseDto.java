package com.easyterview.wingterview.interview.dto.response;

import lombok.*;

@Getter
@Builder
public class AiInterviewResponseDto {
    private final String interviewId;

    public static AiInterviewResponseDto toDto(String interviewId){
        return AiInterviewResponseDto.builder()
                .interviewId(interviewId)
                .build();
    }
}
