package com.bantads.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {

    public static final String CLIENT_QUEUE = "ms.cliente.cmd";
    public static final String CLIENT_QUEUE_DLQ = "ms.cliente.cmd.dlq";
    public static final String ORQUESTRADOR_QUEUE = "orquestrador.reply";
    public static final String EMAIL_QUEUE = "ms.email.cmd";

    private static final int MAX_TENTATIVAS = 3;
    private static final long INTERVALO_RETRY_MS = 5000L;

    @Bean
    public Queue clientQueue() {
        return QueueBuilder.durable(CLIENT_QUEUE)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", CLIENT_QUEUE + ".dlq")
            .build();
    }

    @Bean
    public Queue clientQueueDlq() {
        return QueueBuilder.durable(CLIENT_QUEUE_DLQ).build();
    }

    @Bean
    public Queue orquestradorQueue() {
        return QueueBuilder.durable(ORQUESTRADOR_QUEUE).build();
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }


    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(retryInterceptor());
        return factory;
    }

    private RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(MAX_TENTATIVAS)
                .backOffOptions(INTERVALO_RETRY_MS, 1.0, INTERVALO_RETRY_MS)
                .build();
    }

    private static Queue commandQueue(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", name + ".dlq")
                .build();
    }

    private static Queue dlq(String name) {
        return QueueBuilder.durable(name + ".dlq").build();
    }
}
