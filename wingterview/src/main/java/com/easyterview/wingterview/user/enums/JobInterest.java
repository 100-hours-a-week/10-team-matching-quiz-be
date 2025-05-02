package com.easyterview.wingterview.user.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum JobInterest {

    BACKEND_DEVELOPER("백엔드 개발자"),
    FRONTEND_DEVELOPER("프론트엔드 개발자"),
    FULLSTACK_DEVELOPER("풀스택 개발자"),
    CLOUD_ENGINEER("클라우드 엔지니어"),
    SOLUTIONS_ARCHITECT("솔루션즈 아키텍트"),
    DEVOPS_ENGINEER("DevOps 엔지니어"),
    ML_ENGINEER("머신러닝 엔지니어"),
    AI_BACKEND_DEVELOPER("AI 백엔드 개발자"),
    DATA_SCIENTIST("데이터 사이언티스트");

    private final String label;

    JobInterest(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static JobInterest from(String label) {
        return Stream.of(JobInterest.values())
                .filter(j -> j.label.equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown job interest: " + label));
    }
}
