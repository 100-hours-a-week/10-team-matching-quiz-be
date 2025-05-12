package com.easyterview.wingterview.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BlockedSeats {
    private final boolean[][] A;
    private final boolean[][] B;
    private final boolean[][] C;
}
