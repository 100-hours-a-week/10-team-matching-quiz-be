package com.easyterview.wingterview.global.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("잘못된 토큰입니다.");
    }
}
