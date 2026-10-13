package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.entity.Rabbit;
import com.bantads.handlers.SagaStepHandler;
import com.bantads.model.RabbitAnswer;
import com.bantads.model.RabbitRequest;
import com.bantads.entity.Request;
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

    private final Map<String, SagaStepHandler> handlers;

    public SagaService(List<SagaStepHandler> handlersToInject) {
        this.handlers = handlersToInject.stream()
                .collect(Collectors.toMap(SagaStepHandler::action, h -> h));
    }

    @RabbitListener(queues = RabbitMQConfig.CLIENT_QUEUE)
    public void handleClientCQueue(RabbitRequest command) {

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
                handler.compensate(command.getPayload());
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

        RabbitAnswer answer = RabbitAnswer.builder()
                .sagaId(command.getSagaId())
                .type(command.getType())
                .error(error)
                .status(status)
                .stepIndex(command.getStepIndex())
                .build();

        Map<String, Object> reply = new HashMap<>();
        reply.put("sagaId", answer.getSagaId());
        reply.put("type", answer.getType());
        reply.put("stepIndex", answer.getStepIndex());
        reply.put("attempt", command.getAttempt());
        reply.put("status", status.toString());
        reply.put("error", error);
        reply.put("timestamp", new Date());
        reply.put("payload", payload);

        this.sendCommand(reply);
        if (status == Status.SUCESSO) {
            rabbitRepository.save(req);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CLIENT_QUEUE_DLQ)
    public void handlDlqQueue(Map<String, Object> command) {
        UUID sagaId = UUID.fromString(command.get("sagaId").toString());
        String type = command.get("type").toString();

        boolean alreadyProcessed = rabbitRepository.existsBySagaIdAndType(sagaId, type);
        if (alreadyProcessed) {
            return;
        }

        Rabbit req = Rabbit.builder()
                .sagaId(sagaId)
                .type(type)
                .build();


        if(type.contains("criar-cliente")) {
            this.clientService.deleteClient(command);
        }

        if(type.contains("aprovar-solicitacao")) {
            String id = (String) command.get("idSolicitacao");
            this.requestService.compensateRequestStatus(id);
        }

        rabbitRepository.save(req);
    }

    public void sendCommand(Map<String, Object> answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
