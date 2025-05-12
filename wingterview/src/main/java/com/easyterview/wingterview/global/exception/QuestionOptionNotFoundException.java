package com.easyterview.wingterview.global.exception;

public class QuestionOptionNotFoundException extends RuntimeException {
    public QuestionOptionNotFoundException() {
        super("질문 목록이 존재하지 않습니다.");
    }
}
