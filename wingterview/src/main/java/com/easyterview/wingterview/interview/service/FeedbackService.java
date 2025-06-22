package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.interview.dto.request.FeedbackRequestDto;

public interface FeedbackService {
    void requestSttFeedback(String userId);

    void sendFeedback(String interviewId, FeedbackRequestDto dto);

}
