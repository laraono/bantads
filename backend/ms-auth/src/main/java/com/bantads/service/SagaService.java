package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.entity.Request;
import com.bantads.handlers.SagaStepHandler;
import com.bantads.model.RabbitRequest;
import com.bantads.model.Status;
import com.bantads.repository.RequestRepository;
import org.springframework.web.server.ResponseStatusException;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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

        if (this.alreadyProcessed(command.getSagaId(), command.getType())) {
            return;
        }

        SagaStepHandler handler = this.handlers.get(command.getType());

        if (handler == null) {
            throw new RuntimeException("Handler não encontrado para:" + command.getType());
        }

        Map<String, Object> payload = command.getPayload();
        Status status = Status.SUCESSO;
        String error = null;

        try {
            payload = handler.execute(command.getPayload());
        } catch (Exception e) {
            status = Status.FALHA;
            error = (e instanceof ResponseStatusException rse && rse.getReason() != null) ? rse.getReason()
                    : e.getMessage();
            e.printStackTrace();
        }

        if (this.alreadyProcessed(command.getSagaId(), command.getType())) {
            if (status == Status.SUCESSO) {
                handler.compensate(command.getPayload());
            }
            return;
        }

        Request req = Request.builder()
                .id(new ObjectId())
                .sagaId(command.getSagaId())
                .type(command.getType())
                .compensation(command.isCompensation())
                .build();

        command.setPayload(payload);
        command.setStatus(status.toString());
        command.setError(error);
        command.setTimestamp(new Date());

        requestRepository.save(req);

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, command);
    }

    @RabbitListener(queues = RabbitMQConfig.AUTH_QUEUE_DLQ)
    public void handleDlqQueue(RabbitRequest command) {
        UUID sagaId = command.getSagaId();
        String type = command.getType();

        Map<String, Object> payload = command.getPayload();

        if (requestRepository.existsBySagaIdAndTypeAndCompensation(sagaId, type + ".comp", true)) {
            return;
        }

        Request marker = requestRepository.save(Request.builder()
                // é útil pra sinalizar o quanto antes que uma compensação está acontecendo
                // para uma msg válida
                .id(new ObjectId())
                .sagaId(sagaId)
                .type(type + ".comp")
                .compensation(true)
                .build());

        SagaStepHandler handler = this.handlers.get(type);

        try {
            if (handler != null) {
                handler.compensate(payload);
            }
        } catch (RuntimeException e) {
            requestRepository.delete(marker); // se der erro ainda dá pra refazer a compensação
            throw e;
        }
    }

    // no stop e retorno, pode acontecer da msg ser consumida e armazenada mesmo dps
    // de todos os que vinham antes compensarem
    private boolean alreadyProcessed(UUID sagaId, String type) {
        return requestRepository.existsBySagaIdAndType(sagaId, type)
                || requestRepository.existsBySagaIdAndType(sagaId, type + ".comp");
    }
}
