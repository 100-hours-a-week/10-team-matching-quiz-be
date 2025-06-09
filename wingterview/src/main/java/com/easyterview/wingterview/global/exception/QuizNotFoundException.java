package com.easyterview.wingterview.global.exception;

public class QuizNotFoundException extends RuntimeException {
    public QuizNotFoundException() {
        super("퀴즈가 존재하지 않습니다.");
    }
}
