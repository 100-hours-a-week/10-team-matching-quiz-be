package com.easyterview.wingterview.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDto {
    private final String accessToken;
    private final String refreshToken;
    private final Boolean isNewUser;
}
