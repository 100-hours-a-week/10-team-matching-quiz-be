package com.easyterview.wingterview.global.exception;

public class UserNotParticipatedException extends RuntimeException {
    public UserNotParticipatedException() {
        super("큐에 존재하지 않는 사용자");
    }
}
