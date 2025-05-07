package com.easyterview.wingterview.interview.controller;

import com.easyterview.wingterview.common.constants.InterviewResponseMessage;
import com.easyterview.wingterview.global.response.ApiResponse;
import com.easyterview.wingterview.interview.dto.response.NextRoundDto;
import com.easyterview.wingterview.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PutMapping("/{interviewId}/status/next")
    public ResponseEntity<ApiResponse> goNextStage(@PathVariable String interviewId){
        NextRoundDto nextRoundDto = interviewService.goNextStage(interviewId);
        return ApiResponse.response(InterviewResponseMessage.INTERVIEW_PHASE_UPDATED,nextRoundDto);
    }
}
