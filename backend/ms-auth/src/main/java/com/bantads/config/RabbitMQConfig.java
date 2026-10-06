package com.bantads.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String AUTH_QUEUE = "ms.auth.cmd";
    public static final String AUTH_QUEUE_DLQ = "ms.auth.cmd.dlq";
    public static final String ORQUESTRADOR_QUEUE = "orquestrador.reply";

    @Bean
    public Queue authQueue() {
        return QueueBuilder.durable(AUTH_QUEUE)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", AUTH_QUEUE + ".dlq")
            .build();
    }

    @Bean
    public Queue authQueueDlq() {
        return QueueBuilder.durable(AUTH_QUEUE_DLQ).build();
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
