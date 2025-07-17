package com.easyterview.wingterview.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserCountController {

    private final SessionCounter sessionCounter;

    @GetMapping("/api/active-users")
    public int getActiveUsers() {
        return sessionCounter.getActiveSessionCount();
    }
}