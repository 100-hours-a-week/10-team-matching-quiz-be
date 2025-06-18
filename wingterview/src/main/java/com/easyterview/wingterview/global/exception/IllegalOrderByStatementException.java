package com.easyterview.wingterview.global.exception;

public class IllegalOrderByStatementException extends RuntimeException {
    public IllegalOrderByStatementException() {
        super("잘못된 정렬 기준");
    }
}
