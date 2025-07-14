package com.easyterview.wingterview.user.dto.request;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class UserUpdateRequestDto {
    private String name;
    private String nickname;
    private List<String> jobInterest;
    private List<String> techStack;
    private String profileImageUrl;
    private SeatPosition seatPosition;
}
