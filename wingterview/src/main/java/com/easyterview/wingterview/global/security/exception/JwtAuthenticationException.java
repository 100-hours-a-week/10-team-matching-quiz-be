package com.easyterview.wingterview.global.security.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class JwtAuthenticationException extends AuthenticationException {
    private final int statusCode;

    public JwtAuthenticationException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;
    }

}
