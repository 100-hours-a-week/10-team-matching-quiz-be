package com.easyterview.wingterview.user.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum TechStack {

    JAVA("Java"),
    SPRING("Spring"),
    REACT("React"),
    PYTHON("Python"),
    KUBERNETES("Kubernetes"),
    AWS("AWS"),
    PYTORCH("Pytorch"),
    FASTAPI("Fastapi"),
    LANGCHAIN("Langchain"),
    MLOPS("MLOps"),
    DEVOPS("DevOps"),
    DATABASE("데이터베이스"),
    SECURITY("보안"),
    SOFTWARE_ARCHITECTURE("소프트웨어 아키텍처");

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
