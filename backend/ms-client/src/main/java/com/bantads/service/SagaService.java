package com.bantads.service;

import com.bantads.handlers.SagaStepHandler;
import com.bantads.config.RabbitMQConfig;
import com.bantads.entity.Rabbit;
import com.bantads.entity.Request;
import com.bantads.model.RabbitAnswer;
import com.bantads.model.RabbitRequest;
import com.bantads.model.Status;
import com.bantads.repository.RabbitRepository;
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
    private ClientService clientService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitRepository rabbitRepository;

    private Map<String, SagaStepHandler> handlers;

    public SagaService(List<SagaStepHandler> handlersToInject) {
        this.handlers = handlersToInject.stream()
                .collect(Collectors.toMap(SagaStepHandler::action, h -> h));
    }

    @RabbitListener(queues = RabbitMQConfig.CLIENT_QUEUE)
    public void handleClientQueue(RabbitRequest command) {

        boolean alreadyProcessed = rabbitRepository.existsBySagaIdAndTypeAndCompensation(command.getSagaId(),
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
                handler.compensate(command.getPayload(), command.getError());
            } else {
                payload = handler.execute(command.getPayload());
                status = Status.SUCESSO;
            }
        } catch (Exception e) {
            status = Status.FALHA;
            error = e.getMessage();
            e.printStackTrace();
        }
        Rabbit req = Rabbit.builder()
                .sagaId(command.getSagaId())
                .type(command.getType())
                .compensation(command.isCompensation())
                .build();

        command.setStatus(status.toString());
        command.setError(error);
        command.setTimestamp(new Date());
        command.setPayload(payload);

        this.sendCommand(command);
        if (status == Status.SUCESSO) {
            rabbitRepository.save(req);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CLIENT_QUEUE_DLQ)
    public void handlDlqQueue(RabbitRequest command) {
        UUID sagaId = command.getSagaId();
        String type = command.getType();
        Map<String, Object> payload = command.getPayload();

        boolean alreadyProcessed = rabbitRepository.existsBySagaIdAndType(sagaId, type);
        if (alreadyProcessed) {
            return;
        }

        SagaStepHandler handler = this.handlers.get(type);

        if (handler != null) {
            handler.compensate(payload, command.getError());
        }

        Rabbit req = Rabbit.builder()
                .sagaId(sagaId)
                .type(type)
                .build();

        rabbitRepository.save(req);
    }


    public void sendCommand(RabbitRequest answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
