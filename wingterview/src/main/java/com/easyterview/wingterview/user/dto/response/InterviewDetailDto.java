package com.easyterview.wingterview.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Builder
public class InterviewDetailDto {
    private Timestamp createdAt;
    private Long duration;
    private String recordingUrl;
    private List<FeedbackItem> feedback;
}
