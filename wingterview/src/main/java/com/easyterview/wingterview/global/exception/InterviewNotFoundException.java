package com.easyterview.wingterview.global.exception;

public class InterviewNotFoundException extends RuntimeException {
    public InterviewNotFoundException() {
        super("인터뷰가 존재하지 않음");
    }
}
