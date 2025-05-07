package com.easyterview.wingterview.interview.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionCreationResponseDto {
    private final List<String> questions;
}
