package com.easyterview.wingterview.global.exception;

public class AlreadyBlockedSeatException extends RuntimeException {
    public AlreadyBlockedSeatException() {
        super("이미 막힌 자리");
    }
}
