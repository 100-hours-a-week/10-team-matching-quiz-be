package com.easyterview.wingterview.s3.controller;

import com.easyterview.wingterview.common.constants.S3ResponseMessage;
import com.easyterview.wingterview.global.response.BaseResponse;
import com.easyterview.wingterview.s3.dto.UploadUrlDto;
import com.easyterview.wingterview.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.time.Duration;

@Slf4j
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@Tag(name = "S3 API", description = "프로필 이미지 Presigned URL 발급 API")
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "Presigned URL 발급", description = "S3에 업로드할 수 있는 URL을 발급합니다.")
    @GetMapping("/presigned-url")
    public ResponseEntity<BaseResponse> getPresignedUrl(
            @RequestParam String filename
    ) {

        URL url = s3Service.generatePresignedUrl(filename, Duration.ofMinutes(5));
        return BaseResponse.response(S3ResponseMessage.URL_FETCH_DONE, UploadUrlDto.builder().url(url.toString()).build());
    }


    // 저장해놓은 url은 분석 이후 지우는 로직 적용 필요
    @Operation(summary = "녹음파일 url 저장", description = "S3에 업로드할 수 있는 URL을 발급합니다.")
    @PostMapping("/presigned-url/recording")
    public ResponseEntity<BaseResponse> saveRecordingUrl(@RequestParam String filename){
        s3Service.saveRecordingUrl(filename);
        return BaseResponse.response(S3ResponseMessage.URL_SAVE_DONE);
    }

    @DeleteMapping("/presigned-url/recording")
    public ResponseEntity<BaseResponse> deleteRecording(@RequestParam String url){
        s3Service.deleteS3ObjectByUrl(url);
        return BaseResponse.response(S3ResponseMessage.URL_SAVE_DONE);
    }
}
