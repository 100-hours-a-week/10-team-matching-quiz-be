package com.easyterview.wingterview.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq")
public record RabbitMqProperties(
        QueueProps queue,
        ExchangeProps exchange,
        RoutingProps routing
) {
    public record QueueProps(String name) {}
    public record ExchangeProps(String name) {}
    public record RoutingProps(String key) {}
}