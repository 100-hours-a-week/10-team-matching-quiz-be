package com.easyterview.wingterview.global.exception;

import com.easyterview.wingterview.common.constants.ExceptionMessage;
import com.easyterview.wingterview.global.response.BaseResponse;
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
    public ResponseEntity<BaseResponse> validityException(MethodArgumentNotValidException e){
        // 에러 메시지 추출
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.error(errorMessage);
        return BaseResponse.response(ExceptionMessage.INVALID_INPUT, CustomExceptionDto.builder().reason(errorMessage).build());
    }

    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<BaseResponse> handleTypeMismatchException(UnexpectedTypeException e) {
        log.error(e.getMessage());
        return BaseResponse.response(ExceptionMessage.INVALID_INPUT, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse> handleIllegalArgument(IllegalArgumentException e) {
        return BaseResponse.response(ExceptionMessage.INVALID_INPUT, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(AlreadyEnqueuedException.class)
    public ResponseEntity<BaseResponse> handleAlreadyEnqueued(AlreadyEnqueuedException e) {
        return BaseResponse.response(ExceptionMessage.ALREADY_ENQUEUED, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(MatchingClosedException.class)
    public ResponseEntity<BaseResponse> handleMatchingClosed(MatchingClosedException e) {
        return BaseResponse.response(ExceptionMessage.QUEUE_CLOSED, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(UserNotParticipatedException.class)
    public ResponseEntity<BaseResponse> handleUserNotFound(UserNotParticipatedException e){
        return BaseResponse.response(ExceptionMessage.INVALID_USER, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(AlreadyBlockedSeatException.class)
    public ResponseEntity<BaseResponse> handleAlreadyBlockedSeat(AlreadyBlockedSeatException e){
        return BaseResponse.response(ExceptionMessage.ALREADY_BLOCKED_SEAT, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }


    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<BaseResponse> handleInvalidToken(InvalidTokenException e){
        // 에러 메시지 추출
        String errorMessage = e.getMessage();
        log.error(errorMessage);
        return BaseResponse.response(ExceptionMessage.INVALID_TOKEN, CustomExceptionDto.builder().reason(errorMessage).build());
    }

    @ExceptionHandler(InvalidUUIDFormatException.class)
    public ResponseEntity<BaseResponse> handleInvalidUUIDFormat(InvalidUUIDFormatException e){
        return BaseResponse.response(ExceptionMessage.INVALID_UUID, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }


    @ExceptionHandler(InterviewNotFoundException.class)
    public ResponseEntity<BaseResponse> handleInterviewNotFound(InterviewNotFoundException e){
        return BaseResponse.response(ExceptionMessage.INTERVIEW_NOT_FOUND, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(QuestionOptionNotFoundException.class)
    public ResponseEntity<BaseResponse> handleQuestionNotFound(QuestionOptionNotFoundException e){
        return BaseResponse.response(ExceptionMessage.QUESTION_NOT_FOUND, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseResponse> handleUserNotFound(UserNotFoundException e){
        return BaseResponse.response(ExceptionMessage.USER_NOT_FOUND, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }

    @ExceptionHandler(IllegalFileFormatException.class)
    public ResponseEntity<BaseResponse> handleIllegalFileFormat(IllegalFileFormatException e){
        return BaseResponse.response(ExceptionMessage.INVALID_FILE_FORMAT, CustomExceptionDto.builder().reason(e.getMessage()).build());
    }
}
