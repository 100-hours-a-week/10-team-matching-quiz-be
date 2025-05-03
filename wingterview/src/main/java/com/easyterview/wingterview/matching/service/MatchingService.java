package com.easyterview.wingterview.matching.service;

import com.easyterview.wingterview.matching.dto.request.MatchingResultDto;

public interface MatchingService {
    MatchingResultDto getMatchingResult();
    void enqueue();
}
