package com.easyterview.wingterview.rabbitmq.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@ToString
public class MainQuestionRequestMessage {
    private final String interviewId;
    private final List<String> questions;
    private final LocalDateTime requestedAt;
}
