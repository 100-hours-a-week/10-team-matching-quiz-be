package com.easyterview.wingterview.interview.controller;

import com.easyterview.wingterview.common.constants.InterviewResponseMessage;
import com.easyterview.wingterview.global.response.ApiResponse;
import com.easyterview.wingterview.interview.dto.request.FeedbackRequestDto;
import com.easyterview.wingterview.interview.dto.request.QuestionCreationRequestDto;
import com.easyterview.wingterview.interview.dto.request.QuestionSelectionRequestDto;
import com.easyterview.wingterview.interview.dto.response.InterviewStatusDto;
import com.easyterview.wingterview.interview.dto.response.NextRoundDto;
import com.easyterview.wingterview.interview.dto.response.QuestionCreationResponseDto;
import com.easyterview.wingterview.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/status")
    public ResponseEntity<ApiResponse> getInterviewStatus(){
       InterviewStatusDto interviewStatusDto = interviewService.getInterviewStatus();
       return ApiResponse.response(InterviewResponseMessage.INTERVIEW_PHASE_FETCH_DONE,interviewStatusDto);
    }

    @PostMapping("/{interviewId}/question")
    public ResponseEntity<ApiResponse> makeQuestion(@PathVariable String interviewId, @RequestBody QuestionCreationRequestDto dto){
        QuestionCreationResponseDto responseDto = interviewService.makeQuestion(interviewId, dto);
        return ApiResponse.response(InterviewResponseMessage.QUESTION_FETCH_DONE, responseDto);
    }

    @PostMapping("/{interviewId}/selection")
    public ResponseEntity<ApiResponse> selectQuestion(@PathVariable String interviewId, @RequestBody QuestionSelectionRequestDto dto){
        interviewService.selectQuestion(interviewId, dto);
        return ApiResponse.response(InterviewResponseMessage.QUESTION_SELECT_DONE);
    }

    @PostMapping("/{interviewId}/feedback")
    public ResponseEntity<ApiResponse> sendFeedback(@PathVariable String interviewId, @RequestBody FeedbackRequestDto dto){
        interviewService.sendFeedback(interviewId, dto);
        return ApiResponse.response(InterviewResponseMessage.FEEDBACK_SEND_DONE);
    }
}
