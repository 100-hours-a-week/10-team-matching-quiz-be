package com.easyterview.wingterview.s3.service;

import java.net.URL;
import java.time.Duration;

public interface S3Service {
    URL generatePresignedUrl(String objectKey, Duration expiration);
    void saveProfileImageUrl(String objectKey);
}
