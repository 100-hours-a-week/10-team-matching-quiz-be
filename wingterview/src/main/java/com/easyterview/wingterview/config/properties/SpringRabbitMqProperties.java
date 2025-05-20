package com.easyterview.wingterview.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.rabbitmq")
public record SpringRabbitMqProperties(
        String host,
        int port,
        String username,
        String password
) {}
