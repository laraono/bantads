package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.entity.event.Event;
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

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_QUEUE)
    public void handleAccountQueue(Map<String, Object> payload) {

        UUID sagaId = UUID.fromString(payload.get("sagaId").toString());
        String type = payload.get("type").toString();

        boolean alreadyProcessed = requestRepository.existsBySagaIdAndType(sagaId, type);

        if (alreadyProcessed) {
            return;
        }

        payload.put("status", Status.SUCESSO);
        payload.put("timestamp", new Date());

        Request req = Request.builder()
                .sagaId(sagaId)
                .type(type)
                .build();

        if(type.contains("decidir-gerente")) {
            try {
                String cpf = this.accountService.findManager(payload);
                payload.put("cpfGerente", cpf);

                requestRepository.save(req);
            } catch (Exception e) {
                payload.put("status", Status.FALHA);
                payload.put("timestamp", new Date());
                e.printStackTrace();
                throw e;
            }
        }

        if(type.contains("associar-gerente")) {
            try {
                Map<String, String> gerente = (Map<String, String>) payload.get("gerente");
                String managerCpf = gerente.get("cpf");
                String accountNumber = (String) payload.get("numeroConta");
                this.eventService.updateManager(managerCpf, accountNumber);
                requestRepository.save(req);
            } catch (Exception e) {
                payload.put("status", Status.FALHA);
                payload.put("timestamp", new Date());
                e.printStackTrace();
                throw e;
            }
        }

        if(type.contains("identificar-conta")) {
            try {
                this.accountService.getAccountToNewManager(payload);
                requestRepository.save(req);
            } catch (Exception e) {
                payload.put("status", Status.FALHA);
                payload.put("timestamp", new Date());
                e.printStackTrace();
                throw e;
            }
        }

        if(type.contains("criar-conta")) {
            try {
                String cpfGerente = String.valueOf(payload.get("cpfGerente"));
                String cpfCliente = String.valueOf(payload.get("cpfCliente"));

                Map<String, Object> eventPayload = new HashMap<>();
                eventPayload.put("cpfGerente", cpfGerente);
                eventPayload.put("cpfCliente", cpfCliente);

                Event event = this.eventService.createAccount(eventPayload);

                eventPayload.put("numeroConta", event.getObjectId());

                requestRepository.save(req);
            } catch (Exception e) {
                payload.put("status", Status.FALHA);
                payload.put("timestamp", new Date());
                e.printStackTrace();
                throw e;
            }
        }

        this.sendCommand(payload);

    }

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_QUEUE_DLQ)
    public void handleDlq(Map<String, Object> command) {
        UUID sagaId = UUID.fromString(command.get("sagaId").toString());
        String type = command.get("type").toString();

        boolean alreadyProcessed = requestRepository.existsBySagaIdAndType(sagaId, type);
        if (alreadyProcessed) {
            return;
        }

        Request req = Request.builder()
                .sagaId(sagaId)
                .type(type)
                .build();

        if(type.contains("criar-conta")) {
            String cpfCliente = String.valueOf(command.get("cpf"));
            String numeroConta = String.valueOf(command.get("numeroConta"));

            this.accountService.deleteAccountByCPF(cpfCliente);
            this.eventService.deleteByObjectId(numeroConta);
            requestRepository.save(req);
        }

        if(type.contains("associar-gerente")) {
            String managerCpf = (String) command.get("gerenteAntigo");
            String accountNumber = (String) command.get("numeroConta");
            this.eventService.updateManager(managerCpf, accountNumber);
            requestRepository.save(req);
        }

    }

    public void sendCommand(Map<String, Object> answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
