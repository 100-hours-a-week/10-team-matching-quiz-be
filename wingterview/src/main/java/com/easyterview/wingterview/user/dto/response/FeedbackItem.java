package com.easyterview.wingterview.user.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedbackItem {
    private String question;
    private String modelAnswer;
    private String commentary;
    private Integer startAt;
    private Integer endAt;
}
