package com.easyterview.wingterview.global.exception;

public class AlreadyEnqueuedException extends RuntimeException {
    public AlreadyEnqueuedException() {
        super("이미 진입한 사용자입니다.");
    }
}
