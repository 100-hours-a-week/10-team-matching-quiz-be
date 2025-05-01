package com.easyterview.wingterview.auth.controller;

import com.easyterview.wingterview.auth.dto.request.AuthRequestDto;
import com.easyterview.wingterview.auth.dto.response.AuthResponseDto;
import com.easyterview.wingterview.auth.jwt.JwtUtil;
import com.easyterview.wingterview.auth.service.AuthService;
import com.easyterview.wingterview.common.constants.AuthResponseMessage;
import com.easyterview.wingterview.common.constants.ExceptionMessage;
import com.easyterview.wingterview.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/oauth/{provider}")
    public ResponseEntity<ApiResponse> authLogin(@PathVariable String provider, @RequestBody AuthRequestDto authRequest){

        // 카카오가 아닌 경우 예외처리
        if(!"kakao".equals(provider))
            return ApiResponse.response(AuthResponseMessage.WRONG_PROVIDER);

        Map<String, Object> tokenResponse = authService.getAccessToken(authRequest.getCode());
        AuthResponseDto authResponse = authService.getOrCreateUserByToken(tokenResponse);
        return ApiResponse.response(AuthResponseMessage.AUTHORIZATION_CODE_SEND_DONE,authResponse);
    }

    // test용(프론트엔드 역할)
    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse> kakaoCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription
    ) {
        if (error != null) {
            return ApiResponse.response(AuthResponseMessage.LOGIN_FAILED, errorDescription);
        }

        if (code == null) {
            return ApiResponse.response(AuthResponseMessage.MISSING_AUTH_CODE);
        }

        authLogin("kakao",AuthRequestDto.builder().code(code).build());

        return ApiResponse.response(AuthResponseMessage.LOGIN_SUCCESS);
    }


}
