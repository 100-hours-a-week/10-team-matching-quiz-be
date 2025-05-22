package com.easyterview.wingterview.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


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

    public String getProfileImageUrl(String objectKey) {
        System.out.println("프로필 이미지 url : "+String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, regionName, objectKey));
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, regionName, objectKey);
    }
}

