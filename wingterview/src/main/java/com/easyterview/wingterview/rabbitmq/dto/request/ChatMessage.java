package com.easyterview.wingterview.rabbitmq.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessage {
    private String role;
    private String content;
}
