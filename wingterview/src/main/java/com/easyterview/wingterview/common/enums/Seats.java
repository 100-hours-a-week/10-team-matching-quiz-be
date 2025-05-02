package com.easyterview.wingterview.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Seats {
    ROW_LENGTH(18),
    COL_LENGTH(9)
    ;

    final int length;
}
