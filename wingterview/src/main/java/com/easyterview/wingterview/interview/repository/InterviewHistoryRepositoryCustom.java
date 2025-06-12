package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.user.dto.response.InterviewDetailDto;
import com.easyterview.wingterview.user.dto.response.InterviewHistoryDto;
import com.easyterview.wingterview.user.entity.UserEntity;

import java.util.List;
import java.util.UUID;

public interface InterviewHistoryRepositoryCustom {
    InterviewHistoryDto findByCursorWithLimit(String userId, UUID cursor, Integer limit);

}
