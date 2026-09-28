package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.entity.read.Request;
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
import java.util.Map;

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

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_QUEUE)
    public void handleAccountCommand(RabbitRequest command) {

        boolean alreadyProcessed = requestRepository.existsBySagaIdAndType(command.getSagaId(), command.getType());

        if (alreadyProcessed) {
            return;
        }

        RabbitAnswer answer = RabbitAnswer.builder()
                .sagaId(command.getSagaId())
                .type(command.getType())
                .status(Status.SUCESSO)
                .build();

        Request req = Request.builder()
                .sagaId(command.getSagaId())
                .type(command.getType())
                .build();

        String type = String.valueOf(command.getType());

        Map<String, Object> payload = new HashMap<>();

        if(type.contains("decidir-gerente")) {
            try {
                String cpf = this.accountService.findManager(command.getPayload());
                payload.put("cpf-gerente", cpf);

            } catch (Exception e) {
                answer.setStatus(Status.FALHA);
                answer.setTimestamp(new Date());
                e.printStackTrace();
                throw e;
            }
        }

        if(type.contains("criar-conta")) {
            try {
                this.eventService.createAccount(command.getPayload());
            } catch (Exception e) {
                answer.setStatus(Status.FALHA);
                answer.setTimestamp(new Date());
                e.printStackTrace();
                throw e;
            }
        }

        answer.setPayload(payload);
        answer.setTimestamp(new Date());

        this.sendReadModelCommand(answer);
        requestRepository.save(req);
    }

    public void sendReadModelCommand(RabbitAnswer answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
