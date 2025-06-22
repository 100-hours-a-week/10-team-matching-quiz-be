package com.easyterview.wingterview.interview.service.aiinterview;

import com.easyterview.wingterview.interview.dto.request.TimeInitializeRequestDto;
import com.easyterview.wingterview.interview.dto.response.AiInterviewResponseDto;

public interface AiInterviewService {

    AiInterviewResponseDto startAiInterview(TimeInitializeRequestDto requestDto);
}
