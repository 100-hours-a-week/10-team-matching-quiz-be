package com.easyterview.wingterview.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class AuthResponseDto {
    private final String accessToken;
    private final Boolean isNewUser;
}
