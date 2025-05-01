package com.easyterview.wingterview.global.exception;

import com.easyterview.wingterview.common.constants.ExceptionMessage;
import com.easyterview.wingterview.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> validityException(MethodArgumentNotValidException e){
        // 에러 메시지 추출
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.error(errorMessage);
        return ApiResponse.response(ExceptionMessage.INVALID_INPUT, CustomExceptionDto.builder().reason(errorMessage).build());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse> invalidTokenException(InvalidTokenException e){
        // 에러 메시지 추출
        String errorMessage = e.getMessage();
        log.error(errorMessage);
        return ApiResponse.response(ExceptionMessage.INVALID_TOKEN, CustomExceptionDto.builder().reason(errorMessage).build());
    }
}
