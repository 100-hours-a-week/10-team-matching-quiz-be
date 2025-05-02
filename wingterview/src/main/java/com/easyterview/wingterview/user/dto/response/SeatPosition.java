package com.easyterview.wingterview.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatPosition {
    private final String group;     // A, B, C
    private final int line;         // 1 ~ 18
    private final String position;  // 왼쪽, 중간, 오른쪽
}
