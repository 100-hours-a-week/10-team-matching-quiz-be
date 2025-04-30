package com.easyterview.wingterview.auth.dto.response;

import lombok.Getter;

@Getter
public class TokenResponseDto {
    private String tokenType;
    private String accessToken;
    private String idToken;
    private Integer expiresIn;
    private String refreshToken;
    private Integer refreshTokenExpiresIn;
    private String scope;
}
