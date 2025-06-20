package com.easyterview.wingterview.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class SeatPosition {
    @Pattern(regexp = "^[A-C]$")
    private String section;

    @Size(min = 2, max = 2)
    private List<@Min(1) @Max(18) Integer> seat;
}
