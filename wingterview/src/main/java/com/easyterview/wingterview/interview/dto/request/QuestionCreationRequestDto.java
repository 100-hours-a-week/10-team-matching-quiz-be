package com.easyterview.wingterview.interview.dto.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class QuestionCreationRequestDto {
    private String question;
    private String keywords;
}
