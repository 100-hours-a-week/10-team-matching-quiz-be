package com.easyterview.wingterview.global.response;

import com.easyterview.wingterview.common.constants.ResponseMessage;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class BaseResponse {
    private final String message;
    private final Object data;

    public static BaseResponse of(ResponseMessage responseMessage, @Nullable Object data){
        return BaseResponse.builder()
                .message(responseMessage.getMessage())
                .data(data)
                .build();
    }

    public static BaseResponse of(ResponseMessage responseMessage) {
        return of(responseMessage, null);
    }

    public static ResponseEntity<BaseResponse> response(ResponseMessage responseMessage, @Nullable Object data) {
        return ResponseEntity
                .status(responseMessage.getStatusCode())
                .body(of(responseMessage,data));
    }

    public static ResponseEntity<BaseResponse> response(ResponseMessage responseMessage) {
        return response(responseMessage,null);
    }

}
