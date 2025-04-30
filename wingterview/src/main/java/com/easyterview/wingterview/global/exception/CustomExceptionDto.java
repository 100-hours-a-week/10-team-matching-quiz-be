package com.easyterview.wingterview.global.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomExceptionDto {
    private String reason;
}
