package com.easyterview.wingterview.global.exception;

public class MatchingClosedException extends RuntimeException {
    public MatchingClosedException() {
        super("매칭 큐 진입 시간이 종료되었습니다.");
    }
}
