package com.easyterview.wingterview.matching.algorithm;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MatchingUser {
    private String userId;
    private List<String> jobInterests;
    private List<String> techStacks;
    private String curriculum;
}
