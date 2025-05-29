package com.easyterview.wingterview.rabbitmq.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatRequest {
    private String model;
    private List<ChatMessage> messages;
    private double temperature;
}
