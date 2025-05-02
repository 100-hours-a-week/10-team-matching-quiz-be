package com.easyterview.wingterview.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatPositionDto {
    private final boolean[][] seats;
    private final int[] mySeatPosition;
}
