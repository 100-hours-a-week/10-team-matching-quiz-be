package com.easyterview.wingterview.user.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum TechStack {

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

    TechStack(String label) {
        this.label = label;
    }

    /*
    *  @JsonValue
→ 객체를 JSON으로 직렬화(출력) 할 때 어떤 값을 사용할지 지정
    * */

    @JsonValue
    public String getLabel() {
        return label;
    }

    /*
    * @JsonCreator
→ JSON을 객체로 역직렬화(입력) 할 때 어떤 값을 기준으로 enum을 만들지 지정
    * */
    @JsonCreator
    public static TechStack from(String label) {
        return Stream.of(TechStack.values())
                .filter(t -> t.label.equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown label: " + label));
    }
}
