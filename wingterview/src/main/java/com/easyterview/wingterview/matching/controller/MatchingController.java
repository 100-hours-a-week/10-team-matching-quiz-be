package com.easyterview.wingterview.matching.controller;

import com.easyterview.wingterview.common.constants.MatchingResponseMessage;
import com.easyterview.wingterview.global.response.BaseResponse;
import com.easyterview.wingterview.matching.dto.response.MatchingResultDto;
import com.easyterview.wingterview.matching.dto.response.MatchingStatisticsDto;
import com.easyterview.wingterview.matching.service.MatchingService;
import com.fasterxml.jackson.databind.ser.Serializers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Tag(name = "Matching API", description = "매칭 큐 등록 및 결과 조회 API")
public class MatchingController {

    private final MatchingService matchingService;

    @Operation(summary = "매칭 큐 등록", description = "매칭 큐에 본인을 등록합니다.")
    @PostMapping("/enqueue")
    public ResponseEntity<BaseResponse> enqueueMatching(){
        matchingService.enqueue();
        return BaseResponse.response(MatchingResponseMessage.ENQUEUE_DONE);
    }

    @Operation(summary = "매칭 결과 조회", description = "자신의 매칭 결과를 조회합니다.")
    @GetMapping("/result")
    public ResponseEntity<BaseResponse> getMatchingResult(){
        MatchingResultDto result = matchingService.getMatchingResult();
        if(result == null)
            return BaseResponse.response(MatchingResponseMessage.MATCHING_PENDING);
        return BaseResponse.response(MatchingResponseMessage.MATCHING_RESULT_FETCH_DONE,result);
    }

    @Operation(summary = "매칭 통계 조회", description = "현재 매칭 대기 인원, 누적 매칭 수 등 통계를 조회합니다.")
    @GetMapping("/statistics")
    public ResponseEntity<BaseResponse> getMatchingStatistics(){
        MatchingStatisticsDto statistics = matchingService.getMatchingStatistics();
        return BaseResponse.response(MatchingResponseMessage.MATCHING_STATISTICS_FETCH_DONE,statistics);
    }

    @Operation(summary = "강제 매칭 수행", description = "즉시 매칭 알고리즘을 실행합니다 (관리자용).")
    @PostMapping("/result")
    public ResponseEntity<BaseResponse> doMatching(){
        matchingService.doMatchingAlgorithm();
        return BaseResponse.response(MatchingResponseMessage.MATCHING_DONE);
    }

    @Operation(summary = "매칭 큐 초기화", description = "모든 매칭 큐 참가자를 제거합니다 (관리자용).")
    @DeleteMapping
    public ResponseEntity<BaseResponse> deleteParticipants(){
        matchingService.deleteParticipants();
        return BaseResponse.response(MatchingResponseMessage.MATCHING_DONE);
    }
}
