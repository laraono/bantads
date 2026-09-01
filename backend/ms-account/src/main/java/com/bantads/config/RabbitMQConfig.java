package com.bantads.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "ms.conta.events";

    @Bean
    public Queue readModelQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }
}