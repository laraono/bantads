package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.entity.Request;
import com.bantads.handlers.SagaStepHandler;
import com.bantads.model.RabbitAnswer;
import com.bantads.model.RabbitRequest;
import com.bantads.model.Status;
import com.bantads.repository.RequestRepository;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SagaService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final Map<String, SagaStepHandler> handlers;

    public SagaService(List<SagaStepHandler> handlersToInject) {
        this.handlers = handlersToInject.stream()
                .collect(Collectors.toMap(SagaStepHandler::action, h -> h));
    }

    @RabbitListener(queues = RabbitMQConfig.AUTH_QUEUE)
    public void handleAuthCommand(RabbitRequest command) {

        boolean alreadyProcessed = requestRepository.existsBySagaIdAndTypeAndCompensation(command.getSagaId(),
                command.getType(), command.isCompensation());

        if (alreadyProcessed) {
            return;
        }

        SagaStepHandler handler = this.handlers.get(command.getType());

        if (handler == null) {
            throw new RuntimeException("Handler não encontrado para a ação " + command.getType());
        }

        Map<String, Object> payload = new HashMap<>();
        Status status = Status.SUCESSO;
        String error = null;

        try {
            if (command.isCompensation()) {
                handler.compensate(command.getPayload());
            } else {
                payload = handler.execute(command.getPayload());
            }
        } catch (Exception e) {
            status = Status.FALHA;
            error = e.getMessage();
            e.printStackTrace();
        }

        Request req = Request.builder()
        .id(new ObjectId())
        .sagaId(command.getSagaId())
        .type(command.getType())
        .compensation(command.isCompensation())
        .build();

        requestRepository.save(req);

        RabbitAnswer answer = RabbitAnswer.builder()
                .sagaId(command.getSagaId())
                .type(command.getType())
                .status(status)
                .error(error)
                .stepIndex(command.getStepIndex())
                .attempt(command.getAttempt())
                .payload(payload)
                .timestamp(new Date())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }

    @RabbitListener(queues = RabbitMQConfig.AUTH_QUEUE_DLQ)
    public void handleDlqQueue(Map<String, Object> payload) {
        UUID sagaId = UUID.fromString(payload.get("sagaId").toString());
        String type = payload.get("type").toString();

        if (requestRepository.existsBySagaIdAndType(sagaId, type)) {
            return;
        }

        SagaStepHandler handler = this.handlers.get(type);

        if (handler != null) {
            handler.compensate(payload);
        }

        requestRepository.save(Request.builder()
                .id(new ObjectId())
                .sagaId(sagaId)
                .type(type)
                .build());
    }
}
