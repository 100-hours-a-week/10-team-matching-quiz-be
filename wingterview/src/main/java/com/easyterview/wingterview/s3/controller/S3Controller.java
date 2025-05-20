package com.easyterview.wingterview.s3.controller;

import com.easyterview.wingterview.common.constants.S3ResponseMessage;
import com.easyterview.wingterview.global.response.ApiResponse;
import com.easyterview.wingterview.s3.dto.UploadUrlDto;
import com.easyterview.wingterview.s3.service.S3ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3ServiceImpl s3Service;

    @GetMapping("/presigned-url")
    public ResponseEntity<ApiResponse> getPresignedUrl(@RequestParam String filename) {

        URL url = s3Service.generatePresignedUrl(filename, Duration.ofMinutes(5));
        return ApiResponse.response(S3ResponseMessage.URL_FETCH_DONE, UploadUrlDto.builder().url(url.toString()).build());
    }

//    @PostMapping("/presigned-url")
//    public ResponseEntity<ApiResponse> saveProfileImageUrl(@RequestParam String fileName){
//        s3Service.saveProfileImageUrl(fileName);
//        return ApiResponse.response(S3ResponseMessage.URL_SAVE_DONE);
//    }
}
