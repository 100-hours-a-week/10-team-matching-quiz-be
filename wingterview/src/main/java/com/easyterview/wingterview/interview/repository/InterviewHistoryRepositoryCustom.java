package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.user.dto.response.InterviewHistoryDto;

import java.util.UUID;

public interface InterviewHistoryRepositoryCustom {
    InterviewHistoryDto findByCursorWithLimit(String userId, UUID cursor, Integer limit);

}
