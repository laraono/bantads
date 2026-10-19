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

    public static final String MANAGER_QUEUE = "ms.gerente.cmd";
    public static final String MANAGER_QUEUE_DLQ = "ms.gerente.cmd.dlq";
    public static final String ORQUESTRADOR_QUEUE = "orquestrador.reply";

    private static final int MAX_TENTATIVAS = 3;
    private static final long INTERVALO_RETRY_MS = 5000L;

    @Bean
    public Queue managerQueue() {
        return QueueBuilder.durable(MANAGER_QUEUE)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", MANAGER_QUEUE_DLQ)
            .build();
    }

    // Fila se erros
    @Bean
    public Queue managerQueueDlq() {
        return QueueBuilder.durable(MANAGER_QUEUE_DLQ).build();
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

    // Tenta enviar MAX_TENTATIVAS vezes e se der errado dá dlq
    private RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(MAX_TENTATIVAS)
                .backOffOptions(INTERVALO_RETRY_MS, 1.0, INTERVALO_RETRY_MS)
                .build();
    }
}
