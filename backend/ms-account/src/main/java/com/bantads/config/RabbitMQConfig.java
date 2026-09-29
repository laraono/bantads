package com.bantads.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String EVENT_QUEUE = "ms.conta.events";
    public static final String ACCOUNT_QUEUE = "ms.conta.cmd";
    public static final String ORQUESTRADOR_QUEUE = "orquestrador.reply ";

    @Bean
    public Queue readModelQueue() {
        return QueueBuilder.durable(EVENT_QUEUE).build();
    }

    @Bean
    public Queue accountQueue() {
        return QueueBuilder.durable(ACCOUNT_QUEUE).build();
    }

    @Bean
    public Queue orquestradorQueue() {
        return QueueBuilder.durable(ORQUESTRADOR_QUEUE).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
