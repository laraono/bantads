package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.RequestDTO;
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
import java.util.Map;

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

    @RabbitListener(queues = RabbitMQConfig.CLIENT_QUEUE)
    public void handleAccountCommand(RabbitRequest command) {

        boolean alreadyProcessed = rabbitRepository.existsBySagaIdAndType(command.getSagaId(), command.getType());

        if (alreadyProcessed) {
            return;
        }

        RabbitAnswer answer = RabbitAnswer.builder()
                .sagaId(command.getSagaId())
                .type(command.getType())
                .status(Status.SUCESSO)
                .build();

        Rabbit req = Rabbit.builder()
                .sagaId(command.getSagaId())
                .type(command.getType())
                .build();

        String type = String.valueOf(command.getType());

        Map<String, Object> payload = new HashMap<>();

        try {
            if (type.contains("criar-cliente")) {
                Request request = (Request) command.getPayload().get("requisicao");
                this.clientService.createClient(request);
            }

            if (type.contains("aprovar-solicitacao")) {
                Long id = (Long) command.getPayload().get("idSolicitacao");
                this.requestService.approveRequest(id);
                Request request = this.requestService.getRequestById(id);
                payload.put("cpfCliente", request.getCpf());
            }

            answer.setPayload(payload);
            answer.setTimestamp(new Date());

            this.sendReadModelCommand(answer);
            rabbitRepository.save(req);

        } catch (Exception e) {
            answer.setStatus(Status.FALHA);
            answer.setTimestamp(new Date());
            answer.setPayload(Map.of("error", e.getMessage()));
            this.sendReadModelCommand(answer);
            rabbitRepository.save(req);
            return;
        }
    }

    public void sendReadModelCommand(RabbitAnswer answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
