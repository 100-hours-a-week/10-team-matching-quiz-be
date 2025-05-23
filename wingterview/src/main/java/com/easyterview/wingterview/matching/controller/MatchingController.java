package com.easyterview.wingterview.matching.controller;

import com.easyterview.wingterview.common.constants.MatchingResponseMessage;
import com.easyterview.wingterview.global.response.ApiResponse;
import com.easyterview.wingterview.matching.dto.response.MatchingResultDto;
import com.easyterview.wingterview.matching.dto.response.MatchingStatisticsDto;
import com.easyterview.wingterview.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse> getMatchingStatistics(){
        MatchingStatisticsDto statistics = matchingService.getMatchingStatistics();
        return ApiResponse.response(MatchingResponseMessage.MATCHING_STATISTICS_FETCH_DONE,statistics);
    }

    @PostMapping("/result")
    public ResponseEntity<ApiResponse> doMatching(){
        matchingService.doMatchingAlgorithm();
        return ApiResponse.response(MatchingResponseMessage.MATCHING_DONE);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteParticipants(){
        matchingService.deleteParticipants();
        return ApiResponse.response(MatchingResponseMessage.MATCHING_DONE);
    }
}
