package com.easyterview.wingterview.global.exception;

import com.easyterview.wingterview.common.constants.ExceptionMessage;
import com.easyterview.wingterview.global.response.ApiResponse;
import jakarta.validation.UnexpectedTypeException;
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

    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatchException(UnexpectedTypeException e) {
        log.error(e.getMessage());
        return ApiResponse.response(ExceptionMessage.INVALID_INPUT, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ApiResponse.response(ExceptionMessage.INVALID_INPUT, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(AlreadyEnqueuedException.class)
    public ResponseEntity<ApiResponse> handleAlreadyEnqueued(AlreadyEnqueuedException e) {
        return ApiResponse.response(ExceptionMessage.ALREADY_ENQUEUED, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(MatchingClosedException.class)
    public ResponseEntity<ApiResponse> handleMatchingClosed(MatchingClosedException e) {
        return ApiResponse.response(ExceptionMessage.QUEUE_CLOSED, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(UserNotParticipatedException.class)
    public ResponseEntity<ApiResponse> handleUserNotFound(UserNotParticipatedException e){
        return ApiResponse.response(ExceptionMessage.INVALID_USER, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(AlreadyBlockedSeatException.class)
    public ResponseEntity<ApiResponse> handleAlreadyBlockedSeat(AlreadyBlockedSeatException e){
        return ApiResponse.response(ExceptionMessage.ALREADY_BLOCKED_SEAT, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }


    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse> handleInvalidToken(InvalidTokenException e){
        // 에러 메시지 추출
        String errorMessage = e.getMessage();
        log.error(errorMessage);
        return ApiResponse.response(ExceptionMessage.INVALID_TOKEN, CustomExceptionDto.builder().reason(errorMessage).build());
    }

    @ExceptionHandler(InvalidUUIDFormatException.class)
    public ResponseEntity<ApiResponse> handleInvalidUUIDFormat(InvalidUUIDFormatException e){
        return ApiResponse.response(ExceptionMessage.INVALID_UUID, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }


    @ExceptionHandler(InterviewNotFoundException.class)
    public ResponseEntity<ApiResponse> handleInterviewNotFound(InterviewNotFoundException e){
        return ApiResponse.response(ExceptionMessage.INTERVIEW_NOT_FOUND, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(QuestionOptionNotFoundException.class)
    public ResponseEntity<ApiResponse> handleQuestionNotFound(QuestionOptionNotFoundException e){
        return ApiResponse.response(ExceptionMessage.QUESTION_NOT_FOUND, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFound(UserNotFoundException e){
        return ApiResponse.response(ExceptionMessage.USER_NOT_FOUND, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }
}
