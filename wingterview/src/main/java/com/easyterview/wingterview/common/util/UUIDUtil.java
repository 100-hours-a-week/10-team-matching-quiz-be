package com.easyterview.wingterview.common.util;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class UUIDUtil {

    public static String createUUID() {
        return UUID.randomUUID().toString();
    }

    public static UUID getUserIdFromToken() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
