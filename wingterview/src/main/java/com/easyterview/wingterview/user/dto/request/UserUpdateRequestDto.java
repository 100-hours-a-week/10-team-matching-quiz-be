package com.easyterview.wingterview.user.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class UserUpdateRequestDto {
    private List<String> jobInterest;
    private List<String> techStack;
    private String profileImageUrl;
    private SeatPosition seatPosition;
}
