package com.easyterview.wingterview.interview.service;

import com.easyterview.wingterview.interview.dto.request.TimeInitializeRequestDto;
import com.easyterview.wingterview.interview.dto.response.AiInterviewResponseDto;

public interface AiInterviewService {

    AiInterviewResponseDto startAiInterview(TimeInitializeRequestDto requestDto);
}
