package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.entity.event.Event;
import com.bantads.entity.read.Request;
import com.bantads.handlers.SagaStepHandler;
import com.bantads.model.RabbitAnswer;
import com.bantads.model.RabbitRequest;
import com.bantads.model.Status;
import com.bantads.repository.read.RequestRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class SagaService {
    @Autowired
    private AccountService accountService;

    @Autowired
    private EventService eventService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Map<String, SagaStepHandler> handlers = new HashMap<>();

    public SagaService(List<SagaStepHandler> handlersToInject) {
        this.handlers = handlersToInject.stream()
                .collect(Collectors.toMap(SagaStepHandler::action, h -> h));
    }

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_QUEUE)
    public void handleAccountQueue(RabbitRequest command) {

        boolean alreadyProcessed = requestRepository.existsBySagaIdAndTypeAndCompensation(command.getSagaId(),
                command.getType(), command.isCompensation());

        if (alreadyProcessed) {
            return;
        }

        String action = command.getType();

        if (!this.handlers.containsKey(action)) {
            throw new RuntimeException("Handler não encontrado para a ação " + action);
        }

        SagaStepHandler handler = this.handlers.get(action);

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
        Map<String, Object> reply = new HashMap<>();
        reply.put("sagaId", command.getSagaId());
        reply.put("type", command.getType());
        reply.put("stepIndex", command.getStepIndex());
        reply.put("attempt", command.getAttempt());
        reply.put("status", status.toString());
        reply.put("error", error);
        reply.put("timestamp", new Date());
        reply.put("payload", payload);

        if (status == Status.SUCESSO) {
            requestRepository.save(Request.builder()
                    .sagaId(command.getSagaId())
                    .type(command.getType())
                    .compensation(command.isCompensation())
                    .build());
        }

        this.sendCommand(reply);
    }

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_QUEUE_DLQ)
    public void handleDlq(Map<String, Object> command) {
        UUID sagaId = UUID.fromString(command.get("sagaId").toString());
        String type = command.get("type").toString();

        boolean alreadyProcessed = requestRepository.existsBySagaIdAndTypeAndCompensation(sagaId, type, true);
        if (alreadyProcessed) {
            return;
        }

        SagaStepHandler handler = this.handlers.get(type);

        if (handler != null) {
            handler.compensate(command);
        }

        requestRepository.save(Request.builder()
                .sagaId(sagaId)
                .type(type)
                .compensation(true)
                .build());
    }

    public void sendCommand(Map<String, Object> answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
