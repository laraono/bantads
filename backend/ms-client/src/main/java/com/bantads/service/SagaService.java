package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.RequestDTO;
import com.bantads.entity.Client;
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
import java.util.UUID;

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
    public void handleClientCQueue(Map<String, Object> command) {
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
            try {
                Request request = (Request) command.get("requisicao");
                this.clientService.createClient(request);
                rabbitRepository.save(req);
            } catch (Exception e) {
                command.put("status", Status.FALHA);
                command.put("timestamp", new Date());
                e.printStackTrace();
                throw e;
            }
        }

        if(type.contains("aprovar-solicitacao")) {
            try {
                String id = (String) command.get("idSolicitacao");
                this.requestService.approveRequest(id);
                Request request = this.requestService.getRequestByCpf(id);
                command.put("cpfCliente", request.getCpf());
                command.put("requisicao", request);
                rabbitRepository.save(req);
            } catch (Exception e) {
                command.put("status", Status.FALHA);
                command.put("timestamp", new Date());
                e.printStackTrace();
                throw e;
            }
        }

        if(type.contains("obter-dados")) {
            try {
                String cpf = (String) command.get("cpfCliente");
                Client client = this.clientService.getClient(cpf);

                command.put("nome", client.getName());
                command.put("email", client.getEmail());

                rabbitRepository.save(req);
            } catch (Exception e) {
                command.put("status", Status.FALHA);
                command.put("timestamp", new Date());
                e.printStackTrace();
                throw e;
            }
        }

        command.put("status", "SUCESSO");

        this.sendCommand(command);
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
            String cpf = (String) command.get("cpfCliente");
            this.clientService.deleteByCpf(cpf);
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
