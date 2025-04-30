package com.easyterview.wingterview.common.constants;

import lombok.Getter;

public interface ResponseMessage {
    int getStatusCode();
    String getMessage();
}
