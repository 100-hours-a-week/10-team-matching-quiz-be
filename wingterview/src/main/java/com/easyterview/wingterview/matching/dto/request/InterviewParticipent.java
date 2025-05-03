package com.easyterview.wingterview.matching.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class InterviewParticipent {
    private final String nickname;
    private final String name;
    private final String curriculum;
    private final String profileImageUrl;
    private final List<String> jobInterest;
    private final List<String> techStack;
}
