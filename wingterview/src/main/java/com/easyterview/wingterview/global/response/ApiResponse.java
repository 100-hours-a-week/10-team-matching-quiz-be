package com.easyterview.wingterview.global.response;

import com.easyterview.wingterview.common.constants.ResponseMessage;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class ApiResponse {
    private final String message;
    private final Object data;

    public static ApiResponse of(ResponseMessage responseMessage, @Nullable Object data){
        return ApiResponse.builder()
                .message(responseMessage.getMessage())
                .data(data)
                .build();
    }

    public static ApiResponse of(ResponseMessage responseMessage) {
        return of(responseMessage, null);
    }

    public static ResponseEntity<ApiResponse> response(ResponseMessage responseMessage, @Nullable Object data) {
        return ResponseEntity
                .status(responseMessage.getStatusCode())
                .body(of(responseMessage,data));
    }

    public static ResponseEntity<ApiResponse> response(ResponseMessage responseMessage) {
        return response(responseMessage,null);
    }

}
