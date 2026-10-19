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

        command.setStatus(status.toString());
        command.setError(error);
        command.setPayload(payload);
        command.setTimestamp(new Date());

        if (status == Status.SUCESSO) {
            requestRepository.save(Request.builder()
                    .sagaId(command.getSagaId())
                    .type(command.getType())
                    .compensation(command.isCompensation())
                    .build());
        }

        this.sendCommand(command);
    }

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_QUEUE_DLQ)
    public void handleDlq(RabbitRequest command) {
        UUID sagaId = command.getSagaId();
        String type = command.getType();

        boolean alreadyProcessed = requestRepository.existsBySagaIdAndTypeAndCompensation(sagaId, type, true);
        if (alreadyProcessed) {
            return;
        }

        Map<String, Object> payload = command.getPayload();

        SagaStepHandler handler = this.handlers.get(type);

        if (handler != null) {
            handler.compensate(payload);
        }

        requestRepository.save(Request.builder()
                .sagaId(sagaId)
                .type(type)
                .compensation(true)
                .build());
    }

    public void sendCommand(RabbitRequest answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
