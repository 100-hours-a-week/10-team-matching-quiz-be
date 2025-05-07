package com.easyterview.wingterview.interview.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NextRoundDto {
    private final int currentRound;
    private final String currentPhase;
}
