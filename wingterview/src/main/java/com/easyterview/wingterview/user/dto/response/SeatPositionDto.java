package com.easyterview.wingterview.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatPositionDto {
    private final BlockedSeats seats;
    private final int[] mySeatPosition;
}
