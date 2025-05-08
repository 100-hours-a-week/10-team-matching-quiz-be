package com.easyterview.wingterview.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class UserInfoDto {
    private String nickname;
    private String email;
    private String name;
    private String curriculum;
    private String seatCode;
    private List<String> jobInterest;
    private List<String> techStack;
    private Integer interviewCnt;
    private String profileImageUrl;
}
