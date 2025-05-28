package com.easyterview.wingterview.s3.service;

import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.IllegalFileFormatException;
import com.easyterview.wingterview.global.exception.UserNotFoundException;
import com.easyterview.wingterview.user.entity.RecordingEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.RecordRepository;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;


    @Value("${cloud.aws.region.static}")
    private String regionName;

    @Override
    @Transactional
    public URL generatePresignedUrl(String objectKey, Duration expiration) {
        final String contentType = resolveContentType(objectKey);
        String route = "";
        if(contentType.startsWith("image")){
            route = "profile_image/";
        }
        else if(contentType.startsWith("audio")){
            route = "recording/";
        }
        else{
            throw new IllegalFileFormatException();
        }

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(route+objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(objectRequest)
                .signatureDuration(expiration)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url();
    }

    private String resolveContentType(String filename) {
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".m4a")) return "audio/mp4";
        if (filename.endsWith(".mp3")) return "audio/mpeg";
        // 필요 시 확장
        return "application/octet-stream"; // fallback
    }

    @Transactional
    public void deleteS3ObjectByUrl(String url) {
        String key = extractKeyFromUrl(url); // profile_image/파일명.png

        System.out.println("here is : "+key);

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);

        recordRepository.deleteByUrl(url);
    }

    private String extractKeyFromUrl(String url) {
        // S3 URL에서 key 추출 (공백 등 인코딩된 채로 있어야 URI 파싱 가능)
        try {
            URI uri = new URI(url.replace(" ", "%20"));
            return uri.getPath().substring(1); // 앞에 '/' 제거
        } catch (URISyntaxException e) {
            throw new RuntimeException("❌ URL 파싱 실패: " + e.getMessage());
        }
    }

    @Transactional
    public void saveRecordingUrl(String fileName) {
        final String contentType = resolveContentType(fileName);
        if(!contentType.startsWith("audio")){
            throw new IllegalFileFormatException();
        }

        // 파일 S3 내 경로 지정
        final String objectKey = "recording/" + fileName;

        // full URL 생성
        String recordingUrl = String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                regionName,
                objectKey
        );

        log.info("🎙️ 녹음 파일 URL 저장: {}", recordingUrl);

        recordRepository.save(RecordingEntity.builder()
                        .user(userRepository.findById(UUIDUtil.getUserIdFromToken()).orElseThrow(UserNotFoundException::new))
                        .url(recordingUrl)
                .build());
    }
}
