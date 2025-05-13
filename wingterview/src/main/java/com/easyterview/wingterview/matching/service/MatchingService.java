package com.easyterview.wingterview.matching.service;

import com.easyterview.wingterview.matching.dto.response.MatchingResultDto;
import com.easyterview.wingterview.matching.dto.response.MatchingStatisticsDto;

public interface MatchingService {
    MatchingResultDto getMatchingResult();
    void enqueue();

    MatchingStatisticsDto getMatchingStatistics();
    void doMatchingAlgorithm();

    void deleteParticipants();
}
