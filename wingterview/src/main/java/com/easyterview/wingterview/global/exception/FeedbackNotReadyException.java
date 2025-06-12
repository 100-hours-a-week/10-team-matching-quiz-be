package com.easyterview.wingterview.global.exception;

public class FeedbackNotReadyException extends RuntimeException {
    public FeedbackNotReadyException() {
        super("피드백이 아직 오지 않았음");
    }
}
