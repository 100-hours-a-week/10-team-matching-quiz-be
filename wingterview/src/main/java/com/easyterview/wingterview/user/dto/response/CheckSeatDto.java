package com.easyterview.wingterview.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckSeatDto {
    private final Boolean isSelected;
    private final SeatPosition seatPosition;
}
