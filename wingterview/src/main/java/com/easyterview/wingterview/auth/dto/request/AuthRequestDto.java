package com.easyterview.wingterview.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthRequestDto {
    @Schema(description = "Authorization code")
    private String code;
}
