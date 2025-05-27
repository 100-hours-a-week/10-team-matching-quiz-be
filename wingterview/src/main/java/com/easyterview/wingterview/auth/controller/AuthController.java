package com.easyterview.wingterview.auth.controller;

import com.easyterview.wingterview.auth.dto.request.AuthRequestDto;
import com.easyterview.wingterview.auth.dto.response.AuthResponseDto;
import com.easyterview.wingterview.auth.dto.response.RefreshResponseDto;
import com.easyterview.wingterview.auth.jwt.JwtUtil;
import com.easyterview.wingterview.auth.service.AuthService;
import com.easyterview.wingterview.common.constants.AuthResponseMessage;
import com.easyterview.wingterview.common.constants.ExceptionMessage;
import com.easyterview.wingterview.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "OAuth 로그인 및 토큰 재발급 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "OAuth 로그인", description = "OAuth 인증 코드를 통해 사용자 정보를 조회하거나 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 provider"),
    })
    @PostMapping("/oauth/{provider}")
    public ResponseEntity<BaseResponse> authLogin(@Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider, @RequestBody AuthRequestDto authRequest){

        // 카카오가 아닌 경우 예외처리
        if(!"kakao".equals(provider))
            return BaseResponse.response(AuthResponseMessage.WRONG_PROVIDER);

        Map<String, Object> tokenResponse = authService.getAccessToken(authRequest.getCode());
        AuthResponseDto authResponse = authService.getOrCreateUserByToken(tokenResponse);
        return BaseResponse.response(AuthResponseMessage.AUTHORIZATION_CODE_SEND_DONE,authResponse);
    }

    // test용(프론트엔드 역할)
    /*
    https://kauth.kakao.com/oauth/authorize?response_type=code
&client_id=ef80aa676a39c68abe9ada569d9ab70b
&redirect_uri=http://localhost:8080/api/auth/oauth/kakao/callback

     */
//    @GetMapping("/oauth/kakao/callback")
//    public ResponseEntity<ApiResponse> kakaoCallback(
//            @RequestParam(value = "code", required = false) String code,
//            @RequestParam(value = "error", required = false) String error,
//            @RequestParam(value = "error_description", required = false) String errorDescription
//    ) {
//        if (error != null) {
//            return ApiResponse.response(AuthResponseMessage.LOGIN_FAILED, errorDescription);
//        }
//
//        if (code == null) {
//            return ApiResponse.response(AuthResponseMessage.MISSING_AUTH_CODE);
//        }
//
//        authLogin("kakao",AuthRequestDto.builder().code(code).build());
//
//        return ApiResponse.response(AuthResponseMessage.LOGIN_SUCCESS);
//    }

    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "리프레시 토큰 재발급", description = "AccessToken이 만료되었을 때 RefreshToken을 통해 재발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 유효하지 않음"),
    })
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse> reissueToken(){
        RefreshResponseDto refreshResponseDto = authService.reissue();
        return BaseResponse.response(AuthResponseMessage.REFRESH_TOKEN_DONE, refreshResponseDto);
    }

}
