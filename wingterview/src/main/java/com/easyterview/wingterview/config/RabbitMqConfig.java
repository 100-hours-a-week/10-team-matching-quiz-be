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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@RequiredArgsConstructor
@Configuration
public class RabbitMqConfig {

    private final SpringRabbitMqProperties springRabbitProperties;
    private final RabbitMqProperties rabbitMqProperties;

    // =====================
    // 📤 REQUEST 설정
    // =====================

    @Bean(name = "quizRequestQueue")
    public Queue quizRequestQueue() {
        return new Queue("quiz.request.queue", true);
    }

    @Bean(name = "quizRequestExchange")
    public TopicExchange quizRequestExchange() {
        return new TopicExchange("quiz.request.exchange");
    }

    @Bean(name = "quizRequestBinding")
    public Binding quizRequestBinding(
            @Qualifier("quizRequestQueue") Queue queue,
            @Qualifier("quizRequestExchange") TopicExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with("quiz.request.routingKey");
    }

    @Bean(name = "feedbackRequestQueue")
    public Queue feedbackRequestQueue() {
        return new Queue("feedback.request.queue", true);
    }

    @Bean(name = "feedbackRequestExchange")
    public TopicExchange feedbackRequestExchange() {
        return new TopicExchange("feedback.request.exchange");
    }

    @Bean(name = "feedbackRequestBinding")
    public Binding feedbackRequestBinding(
            @Qualifier("feedbackRequestQueue") Queue queue,
            @Qualifier("feedbackRequestExchange") TopicExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with("feedback.request.routingKey");
    }

    // =====================
    // 📥 RESPONSE 설정
    // =====================

    @Bean(name = "quizResponseQueue")
    public Queue quizResponseQueue() {
        return new Queue("quiz.response.queue", true);
    }

    @Bean(name = "quizResponseExchange")
    public TopicExchange quizResponseExchange() {
        return new TopicExchange("quiz.response.exchange");
    }

    @Bean(name = "quizResponseBinding")
    public Binding quizResponseBinding(
            @Qualifier("quizResponseQueue") Queue queue,
            @Qualifier("quizResponseExchange") TopicExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with("quiz.response.routingKey");
    }

    @Bean(name = "feedbackResponseQueue")
    public Queue feedbackResponseQueue() {
        return new Queue("feedback.response.queue", true);
    }

    @Bean(name = "feedbackResponseExchange")
    public TopicExchange feedbackResponseExchange() {
        return new TopicExchange("feedback.response.exchange");
    }

    @Bean(name = "feedbackResponseBinding")
    public Binding feedbackResponseBinding(
            @Qualifier("feedbackResponseQueue") Queue queue,
            @Qualifier("feedbackResponseExchange") TopicExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with("feedback.response.routingKey");
    }

    // =====================
    // 공통 설정
    // =====================

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(springRabbitProperties.host());
        factory.setPort(springRabbitProperties.port());
        factory.setUsername(springRabbitProperties.username());
        factory.setPassword(springRabbitProperties.password());
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        template.setReplyTimeout(50000); // 50초
        return template;
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory
    ) {
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
                .maxAttempts(5)
                .backOffOptions(2000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }
}
