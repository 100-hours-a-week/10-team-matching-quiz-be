package com.easyterview.wingterview.common.util.mapper.dto;

import com.easyterview.wingterview.common.util.InterviewStatus;
import com.easyterview.wingterview.interview.dto.response.NextRoundDto;

public class NextRoundDtoMapper {
    public static NextRoundDto of(InterviewStatus nextStatus){
        return NextRoundDto.builder()
                .currentPhase(nextStatus.getPhase().getPhase())
                .currentRound(nextStatus.getRound())
                .build();
    }
}
