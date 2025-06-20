package com.easyterview.wingterview.matching.config;

import org.springframework.stereotype.Component;

@Component
public class MatchingStatusManager {
    private volatile boolean isMatchingOpen = true;
    public boolean isMatchingClosed() {
        return !isMatchingOpen;
    }

    public void closeMatching() {
        isMatchingOpen = false;
    }

    public void openMatching() {
        isMatchingOpen = true;
    }
}
