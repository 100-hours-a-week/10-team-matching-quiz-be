package com.easyterview.wingterview.global.exception;

public class BoardNotFoundException extends RuntimeException {
    public BoardNotFoundException() {
        super("게시판이 존재하지 않음");
    }
}
