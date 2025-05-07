package com.easyterview.wingterview.common.util;

import com.easyterview.wingterview.interview.enums.Phase;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewStatus {
    private final Phase phase;
    private final int round;
}
