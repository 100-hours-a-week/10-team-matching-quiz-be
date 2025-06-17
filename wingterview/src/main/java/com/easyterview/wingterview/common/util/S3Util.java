package com.easyterview.wingterview.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Component
public class S3Util {

    private final String bucketName;
    private final String regionName;

    public S3Util(
            @Value("${cloud.aws.s3.bucket}") String bucketName,
            @Value("${cloud.aws.region.static}") String regionName
    ) {
        this.bucketName = bucketName;
        this.regionName = regionName;
    }

    public String getUrl(String objectKey) {
        String encodedKey = URLEncoder.encode(objectKey, StandardCharsets.UTF_8)
                .replace("+", "%20"); // 공백 처리 중요!

        System.out.println("profile_image/"+encodedKey);
        return String.format("https://%s.s3.%s.amazonaws.com/profile_image/%s", bucketName, regionName, encodedKey);
    }
}

