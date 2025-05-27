package com.easyterview.wingterview.s3.controller;

import com.easyterview.wingterview.common.constants.S3ResponseMessage;
import com.easyterview.wingterview.global.response.BaseResponse;
import com.easyterview.wingterview.s3.dto.UploadUrlDto;
import com.easyterview.wingterview.s3.service.S3ServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.time.Duration;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@Tag(name = "S3 API", description = "프로필 이미지 Presigned URL 발급 API")
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3ServiceImpl s3Service;

    @Operation(summary = "Presigned URL 발급", description = "S3에 업로드할 수 있는 URL을 발급합니다.")
    @GetMapping("/presigned-url")
    public ResponseEntity<BaseResponse> getPresignedUrl(
            @RequestParam String filename
    ) {

        URL url = s3Service.generatePresignedUrl(filename, Duration.ofMinutes(5));
        return BaseResponse.response(S3ResponseMessage.URL_FETCH_DONE, UploadUrlDto.builder().url(url.toString()).build());
    }

//    @PostMapping("/presigned-url")
//    public ResponseEntity<ApiResponse> saveProfileImageUrl(@RequestParam String fileName){
//        s3Service.saveProfileImageUrl(fileName);
//        return ApiResponse.response(S3ResponseMessage.URL_SAVE_DONE);
//    }
}
