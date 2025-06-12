package com.easyterview.wingterview.interview.controller;

import com.easyterview.wingterview.common.constants.InterviewResponseMessage;
import com.easyterview.wingterview.global.response.BaseResponse;
import com.easyterview.wingterview.interview.dto.request.*;
import com.easyterview.wingterview.interview.dto.response.*;
import com.easyterview.wingterview.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Tag(name = "Interview API", description = "면접 단계 관리 및 질문 생성 관련 API")
public class InterviewController {

    private final InterviewService interviewService;


    @Operation(summary = "면접 단계 갱신", description = "다음 면접 단계로 상태를 전환합니다.")
    @PutMapping("/{interviewId}/status/next")
    public ResponseEntity<BaseResponse> goNextStage(@PathVariable String interviewId){
        NextRoundDto nextRoundDto = interviewService.goNextStage(interviewId);
        return BaseResponse.response(InterviewResponseMessage.INTERVIEW_PHASE_UPDATED,nextRoundDto);
    }

    @Operation(summary = "면접 상태 조회", description = "현재 면접의 단계와 라운드 정보를 조회합니다.")
    @GetMapping("/status")
    public ResponseEntity<BaseResponse> getInterviewStatus(){
        Object interviewStatusDto = interviewService.getInterviewStatus();
        return BaseResponse.response(InterviewResponseMessage.INTERVIEW_PHASE_FETCH_DONE,interviewStatusDto);
    }

    @Operation(summary = "면접 질문 생성", description = "메인 또는 꼬리질문을 생성합니다.")
    @PostMapping("/{interviewId}/question")
    public ResponseEntity<BaseResponse> makeQuestion(@PathVariable String interviewId, @RequestBody QuestionCreationRequestDto dto){
        Object responseDto = interviewService.makeQuestion(interviewId, dto);
        return BaseResponse.response(InterviewResponseMessage.QUESTION_FETCH_DONE, responseDto);
    }

    @Operation(summary = "면접 질문 선택", description = "제공된 질문 중 선택한 질문을 기록합니다.")
    @PostMapping("/{interviewId}/selection")
    public ResponseEntity<BaseResponse> selectQuestion(@PathVariable String interviewId, @RequestBody QuestionSelectionRequestDto dto){
        interviewService.selectQuestion(interviewId, dto);
        return BaseResponse.response(InterviewResponseMessage.QUESTION_SELECT_DONE);
    }

    @Operation(summary = "피드백 전송", description = "상대방에게 피드백 메시지를 전송합니다.")
    @PostMapping("/{interviewId}/feedback")
    public ResponseEntity<BaseResponse> sendFeedback(@PathVariable String interviewId, @RequestBody FeedbackRequestDto dto){
        interviewService.sendFeedback(interviewId, dto);
        return BaseResponse.response(InterviewResponseMessage.FEEDBACK_SEND_DONE);
    }

    @PostMapping("/ai")
    public ResponseEntity<BaseResponse> startAiInterview(@RequestBody TimeInitializeRequestDto requestDto){
        AiInterviewResponseDto dto = interviewService.startAiInterview(requestDto);
        return BaseResponse.response(InterviewResponseMessage.AI_INTERVIEW_CREATED, dto);
    }

//    @PutMapping("/ai/{interviewId}/time")
//    public ResponseEntity<BaseResponse> initializeInterviewTime(@PathVariable String interviewId, @RequestBody TimeInitializeRequestDto dto){
//        interviewService.initializeInterviewTime(interviewId, dto);
//        return BaseResponse.response(InterviewResponseMessage.INTERVIEW_TIME_INITIALIZED);
//    }

    @DeleteMapping("/{interviewId}")
    public ResponseEntity<BaseResponse> exitInterview(@PathVariable String interviewId){
        interviewService.exitInterview(interviewId);
        return BaseResponse.response(InterviewResponseMessage.INTERVIEW_DELETE_DONE);
    }

//    @PostMapping("/voice/feedback")
//    public ResponseEntity<BaseResponse> getSttFeedback(@PathVariable String userId, @RequestBody FeedbackCallbackDto dto){
//        interviewService.getFeedbackFromAI(userId, dto);
//        return BaseResponse.response(InterviewResponseMessage.FEEDBACK_FETCH_DONE);
//    }
}
