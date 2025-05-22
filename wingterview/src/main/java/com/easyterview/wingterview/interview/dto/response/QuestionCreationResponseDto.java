package com.easyterview.wingterview.interview.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Builder
public class QuestionCreationResponseDto {
    private final List<String> questions;
}
