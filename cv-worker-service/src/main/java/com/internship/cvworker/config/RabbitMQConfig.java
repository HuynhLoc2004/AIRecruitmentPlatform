package com.internship.cvworker.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Value("${app.rabbitmq.dlx}")
    private String dlxExchangeName;

    @Value("${app.rabbitmq.dlq}")
    private String dlqQueueName;

    // 1. Dead Letter Exchange (DLX) Bean
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName);
    }

    // 2. Dead Letter Queue (DLQ) Bean
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqQueueName).build();
    }

    // 3. Bind DLQ to DLX
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(dlqQueueName);
    }

    // 4. Main Topic Exchange Bean
    @Bean
    public TopicExchange mainExchange() {
        return new TopicExchange(exchangeName);
    }

    // 5. Main Queue Bean configured with DLX
    @Bean
    public Queue mainQueue() {
        Map<String, Object> arguments = new HashMap<>();
        // Route failed messages to DLX
        arguments.put("x-dead-letter-exchange", dlxExchangeName);
        arguments.put("x-dead-letter-routing-key", dlqQueueName);
        return QueueBuilder.durable(queueName)
                .withArguments(arguments)
                .build();
    }

    // 6. Bind Main Queue to Main Exchange
    @Bean
    public Binding mainBinding() {
        return BindingBuilder.bind(mainQueue()).to(mainExchange()).with(routingKey);
    }

    // 7. Message Converter for JSON serialization/deserialization
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
