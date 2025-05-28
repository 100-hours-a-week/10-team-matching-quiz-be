package com.easyterview.wingterview.global.exception;

public class IllegalFileFormatException extends RuntimeException {
    public IllegalFileFormatException() {
        super("잘못된 파일 형식입니다.");
    }
}
