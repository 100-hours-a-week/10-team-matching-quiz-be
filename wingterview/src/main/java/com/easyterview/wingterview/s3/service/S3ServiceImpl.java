package com.easyterview.wingterview.s3.service;

import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.UserNotFoundException;
import com.easyterview.wingterview.s3.service.S3Service;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URL;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Presigner s3Presigner;
    private final UserRepository userRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;


    @Value("${cloud.aws.region.static}")
    private String regionName;

    @Override
    public URL generatePresignedUrl(String objectKey, Duration expiration) {
        String contentType = resolveContentType(objectKey);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(objectRequest)
                .signatureDuration(expiration)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url();
    }

    @Override
    public void saveProfileImageUrl(String objectKey) {
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(UserNotFoundException::new);

        String profileImageUrl = String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                regionName,
                objectKey
        );

        log.info("************ {}",profileImageUrl);

        user.setProfileImageUrl(profileImageUrl);
    }

    private String resolveContentType(String filename) {
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        // 필요 시 확장
        return "application/octet-stream"; // fallback
    }
}
