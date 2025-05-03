package com.easyterview.wingterview.matching.controller;

import com.easyterview.wingterview.common.constants.MatchingResponseMessage;
import com.easyterview.wingterview.global.response.ApiResponse;
import com.easyterview.wingterview.matching.dto.request.MatchingResultDto;
import com.easyterview.wingterview.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/enqueue")
    public ResponseEntity<ApiResponse> enqueueMatching(){
        matchingService.enqueue();
        return ApiResponse.response(MatchingResponseMessage.ENQUEUE_DONE);
    }

    @GetMapping("/result")
    public ResponseEntity<ApiResponse> getMatchingResult(){
        MatchingResultDto result = matchingService.getMatchingResult();
        if(result == null)
            return ApiResponse.response(MatchingResponseMessage.MATCHING_PENDING);
        return ApiResponse.response(MatchingResponseMessage.MATCHING_RESULT_FETCH_DONE,result);
    }
}
