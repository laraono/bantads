package com.bantads.orchestration.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String SAGA_CMD = "saga.cmd";
    public static final String ORQUESTRADOR_REPLY = "orquestrador.reply";
    public static final String ORQUESTRADOR_TIMEOUT = "orquestrador.timeout";

    // SAGA_TIMEOUT_MS ajuda na hora de testar as compensações
    public static final int SAGA_TIMEOUT_MS = Integer.parseInt(
            System.getenv().getOrDefault("SAGA_TIMEOUT_MS", "30000"));

    public static final String MS_CLIENTE_CMD = "ms.cliente.cmd";
    public static final String MS_CONTA_CMD = "ms.conta.cmd";
    public static final String MS_GERENTE_CMD = "ms.gerente.cmd";
    public static final String MS_AUTH_CMD = "ms.auth.cmd";
    public static final String MS_EMAIL_CMD = "ms.email.cmd";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // filas sem retry/DLQ 

    @Bean
    public Queue sagaCmdQueue() {
        return QueueBuilder.durable(SAGA_CMD).build();
    }

    @Bean
    public Queue orquestradorReplyQueue() {
        return QueueBuilder.durable(ORQUESTRADOR_REPLY).build();
    }

    @Bean
    public Queue orquestradorTimeoutQueue() {
        return QueueBuilder.durable(ORQUESTRADOR_TIMEOUT).build();
    }
    @Bean
    public Queue msEmailCmdQueue() {
        return QueueBuilder.durable(MS_EMAIL_CMD).build();
    }

    @Bean
    public Queue msEmailCmdWaitQueue() {
        return waitQueue(MS_EMAIL_CMD);
    }

    @Bean
    public Queue msClienteCmdWaitQueue() {
        return waitQueue(MS_CLIENTE_CMD);
    }

    @Bean
    public Queue msContaCmdWaitQueue() {
        return waitQueue(MS_CONTA_CMD);
    }

    @Bean
    public Queue msGerenteCmdWaitQueue() {
        return waitQueue(MS_GERENTE_CMD);
    }

    @Bean
    public Queue msAuthCmdWaitQueue() {
        return waitQueue(MS_AUTH_CMD);
    }

    @Bean
    public Queue msClienteCmdQueue() {
        return commandQueue(MS_CLIENTE_CMD);
    }

    @Bean
    public Queue msClienteCmdDlq() {
        return dlq(MS_CLIENTE_CMD);
    }

    @Bean
    public Queue msContaCmdQueue() {
        return commandQueue(MS_CONTA_CMD);
    }

    @Bean
    public Queue msContaCmdDlq() {
        return dlq(MS_CONTA_CMD);
    }

    @Bean
    public Queue msGerenteCmdQueue() {
        return commandQueue(MS_GERENTE_CMD);
    }

    @Bean
    public Queue msGerenteCmdDlq() {
        return dlq(MS_GERENTE_CMD);
    }

    @Bean
    public Queue msAuthCmdQueue() {
        return commandQueue(MS_AUTH_CMD);
    }

    @Bean
    public Queue msAuthCmdDlq() {
        return dlq(MS_AUTH_CMD);
    }

    //helpers 

    private static Queue commandQueue(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", name + ".dlq")
                .build();
    }


    private static Queue waitQueue(String name) {
        return QueueBuilder.durable(name + ".wait")
                .withArgument("x-message-ttl", SAGA_TIMEOUT_MS)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ORQUESTRADOR_TIMEOUT)
                .build();
    }

    private static Queue dlq(String name) {
        return QueueBuilder.durable(name + ".dlq").build();
    }

}
