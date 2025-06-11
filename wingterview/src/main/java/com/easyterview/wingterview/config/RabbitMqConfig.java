package com.easyterview.wingterview.config;

import com.easyterview.wingterview.config.properties.RabbitMqProperties;
import com.easyterview.wingterview.config.properties.SpringRabbitMqProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@RequiredArgsConstructor
@Configuration
public class RabbitMqConfig {

    private final SpringRabbitMqProperties springRabbitProperties;
    private final RabbitMqProperties rabbitMqProperties;

    // org.springframework.amqp.core.Queue
    @Bean
    public Queue queue() {
        return new Queue(rabbitMqProperties.queue().name());
    }

    /**
     * 지정된 Exchange 이름으로 Topic Exchange Bean 을 생성
     */
    @Bean
    public TopicExchange topicExchange() { return new TopicExchange(rabbitMqProperties.exchange().name());}

    /**
     * 주어진 Queue 와 Exchange 을 Binding 하고 Routing Key 을 이용하여 Binding Bean 생성
     * Exchange 에 Queue 을 등록한다고 이해하자
     **/
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(rabbitMqProperties.routing().key());
    }

    /**
     * RabbitMQ 연동을 위한 ConnectionFactory 빈을 생성하여 반환
     **/
    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(springRabbitProperties.host());
        connectionFactory.setPort(springRabbitProperties.port());
        connectionFactory.setUsername(springRabbitProperties.username());
        connectionFactory.setPassword(springRabbitProperties.password());
        return connectionFactory;
    }

    /**
     * RabbitTemplate
     * ConnectionFactory 로 연결 후 실제 작업을 위한 Template
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());

        // ⏱️ 응답 대기 시간 설정 (단위: ms)
        rabbitTemplate.setReplyTimeout(50000); // 10초까지 기다림

        return rabbitTemplate;
    }

    /**
     * 직렬화(메세지를 JSON 으로 변환하는 Message Converter)
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        factory.setAdviceChain(retryInterceptor());
        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder
                .stateless()
                .maxAttempts(5)                // 최대 재시도 횟수
                .backOffOptions(2000, 2.0, 10000) // 초기 2초, 배수 2.0, 최대 10초까지 증가
                .recoverer(new RejectAndDontRequeueRecoverer()) // 실패 시 재큐하지 않음
                .build();
    }

    @Bean
    public Queue quizResponseQueue() {
        return new Queue("quiz.response.queue", true); // durable = true
    }
}