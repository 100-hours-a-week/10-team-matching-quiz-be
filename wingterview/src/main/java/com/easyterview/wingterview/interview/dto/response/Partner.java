package com.easyterview.wingterview.interview.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Partner {
    private final String nickname;
    private final String name;
    private final String curriculum;
    private final String profileImageUrl;
    private final List<String> jobInterest;
    private final List<String> techStack;
}
