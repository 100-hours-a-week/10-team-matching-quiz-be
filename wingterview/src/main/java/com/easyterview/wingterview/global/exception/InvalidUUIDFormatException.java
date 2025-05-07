package com.easyterview.wingterview.global.exception;

public class InvalidUUIDFormatException extends RuntimeException {
    public InvalidUUIDFormatException() {
        super("UUID 형식이 아님");
    }
}
