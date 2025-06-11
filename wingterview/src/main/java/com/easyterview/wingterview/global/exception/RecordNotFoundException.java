package com.easyterview.wingterview.global.exception;

public class RecordNotFoundException extends RuntimeException {
    public RecordNotFoundException() {
        super("녹음 파일이 존재하지 않습니다.");
    }
}
