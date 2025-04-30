package com.easyterview.wingterview.auth.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthRequestDto {
    private String code;
}
