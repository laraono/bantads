package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.entity.read.Request;
import com.bantads.handlers.SagaStepHandler;
import com.bantads.model.RabbitRequest;
import com.bantads.model.Status;
import com.bantads.repository.read.RequestRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class SagaService {

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
                command.isCompensation() ? command.getType() + ".comp" : command.getType(), command.isCompensation());

        if (alreadyProcessed) {
            return;
        }

        if (!command.isCompensation() && requestRepository.existsBySagaIdAndTypeAndCompensation(
                command.getSagaId(), command.getType() + ".comp", true)) {
            return;
        }

        SagaStepHandler handler = this.handlers.get(command.getType());

        Map<String, Object> payload = command.getPayload();

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
            error = (e instanceof ResponseStatusException rse && rse.getReason() != null) ? rse.getReason()
                    : e.getMessage();
            e.printStackTrace();
        }

        // quando o ms pausa, vem msg, depois despausa, tem a msg .comp e a normal sendo
        // mandadas junto
        // então coloquei isso pra checar se tá compensando
        if (!command.isCompensation() && status == Status.SUCESSO
                && requestRepository.existsBySagaIdAndTypeAndCompensation(
                        command.getSagaId(), command.getType() + ".comp", true)) {
            handler.compensate(payload);
            return;
        }

        command.setStatus(status.toString());
        command.setError(error);
        command.setPayload(payload);
        command.setTimestamp(new Date());

        if (status == Status.SUCESSO) {
            requestRepository.save(Request.builder()
                    .sagaId(command.getSagaId())
                    .type((command.isCompensation() ? command.getType() + ".comp" : command.getType()))
                    .compensation(command.isCompensation())
                    .build());
        }

        this.sendCommand(command);
    }

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_QUEUE_DLQ)
    public void handleDlq(RabbitRequest command) {
        UUID sagaId = command.getSagaId();
        String type = command.getType();

        boolean alreadyProcessed = requestRepository.existsBySagaIdAndTypeAndCompensation(sagaId, type + ".comp", true);
        if (alreadyProcessed) {
            return;
        }

        Map<String, Object> payload = command.getPayload();

        Request req = requestRepository.save(Request.builder()
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
            requestRepository.delete(req); // se der erro ainda dá pra refazer a compensação
            throw e;
        }

    }

    public void sendCommand(RabbitRequest answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
