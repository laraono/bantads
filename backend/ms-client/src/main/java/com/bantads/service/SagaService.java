package com.bantads.service;

import com.bantads.handlers.SagaStepHandler;
import com.bantads.config.RabbitMQConfig;
import com.bantads.entity.Rabbit;
import com.bantads.entity.Request;
import com.bantads.model.RabbitRequest;
import com.bantads.model.Status;
import com.bantads.repository.RabbitRepository;
import org.springframework.web.server.ResponseStatusException;
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
                command.isCompensation() ? command.getType() + ".comp" : command.getType(), command.isCompensation());

        if (alreadyProcessed) {
            return;
        }

        if (!command.isCompensation() && rabbitRepository.existsBySagaIdAndTypeAndCompensation(
                command.getSagaId(), command.getType() + ".comp", true)) {
            return;
        }

        SagaStepHandler handler = this.handlers.get(command.getType());

        Map<String, Object> payload = command.getPayload();

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
            error = (e instanceof ResponseStatusException rse && rse.getReason() != null) ? rse.getReason()
                    : e.getMessage();
            e.printStackTrace();
        }

        // quando o ms pausa, vem msg, depois despausa, tem a msg .comp e a normal sendo
        // mandadas junto
        // então coloquei isso pra checar se tá compensando
        if (!command.isCompensation() && status == Status.SUCESSO
                && rabbitRepository.existsBySagaIdAndTypeAndCompensation(
                        command.getSagaId(), command.getType() + ".comp", true)) {
            handler.compensate(payload);
            return;
        }

        Rabbit req = Rabbit.builder()
                .sagaId(command.getSagaId())
                .type((command.isCompensation() ? command.getType() + ".comp" : command.getType()))
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

        boolean alreadyProcessed = rabbitRepository.existsBySagaIdAndTypeAndCompensation(sagaId, type + ".comp", true);
        if (alreadyProcessed) {
            return;
        }

        Rabbit req = rabbitRepository.save(Rabbit.builder()
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
            rabbitRepository.delete(req); // se der erro ainda dá pra refazer a compensação
            throw e;
        }
    }

    public void sendCommand(RabbitRequest answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
