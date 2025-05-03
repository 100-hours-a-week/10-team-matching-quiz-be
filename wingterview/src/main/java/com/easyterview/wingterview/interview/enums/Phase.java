package com.easyterview.wingterview.interview.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Phase {
    PENDING("pending"),
    PROGRESS("progress"),
    FEEDBACK("feedback"),
    COMPLETE("complete")
    ;

    final String phase;
}
