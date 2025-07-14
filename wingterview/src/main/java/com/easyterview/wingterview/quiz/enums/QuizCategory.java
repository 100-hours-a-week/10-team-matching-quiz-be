package com.easyterview.wingterview.quiz.enums;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum QuizCategory {
    NETWORK("network"),
    DATABASE("database"),
    OS("os"),
    PROGRAMMING("programming"),
    ALGORITHM("algorithm"),
    WEB("web"),
    SECURITY("security"),
    PYTHON("python"),
    HTML("html");

    private final String displayName;

    public static QuizCategory fromDisplayName(String kor) {
        return Arrays.stream(values())
                .filter(c -> c.displayName.equals(kor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown: " + kor));
    }
}
