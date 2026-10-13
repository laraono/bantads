package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
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
    public void handleAccountCommand(RabbitRequest command) {

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
                status = Status.SUCESSO;
            }
        } catch (Exception e) {
            status = Status.FALHA;
            error = e.getMessage();
            e.printStackTrace();
        }
        Request req = Request.builder()
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

        answer.setPayload(payload);
        answer.setTimestamp(new Date());

        this.sendReadModelCommand(answer);
        requestRepository.save(req);
    }

    public void sendReadModelCommand(RabbitAnswer answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
