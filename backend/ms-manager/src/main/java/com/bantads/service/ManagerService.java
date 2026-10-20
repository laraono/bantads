package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.CreateManagerDTO;
import com.bantads.dto.ManagerDTO;
import com.bantads.dto.UpdateManagerDTO;
import com.bantads.entity.Manager;
import com.bantads.entity.Request;
import com.bantads.handlers.SagaStepHandler;
import com.bantads.model.RabbitRequest;
import com.bantads.model.Status;
import com.bantads.repository.ManagerRepository;
import com.bantads.repository.RequestRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ManagerService {

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Map<String, SagaStepHandler> handlers = new HashMap<>();

    @Autowired
    public void setHandlers(List<SagaStepHandler> handlersToInject) {
        this.handlers = handlersToInject.stream()
                .collect(Collectors.toMap(SagaStepHandler::action, h -> h));
    }

    public ManagerDTO createManager(CreateManagerDTO createManagerDTO) {
        Manager manager = Manager.builder()
                .cpf(createManagerDTO.getCpf())
                .email(createManagerDTO.getEmail())
                .name(createManagerDTO.getNome())
                .phone(createManagerDTO.getTelefone())
                .isActive(true)
                .build();

        ManagerDTO managerDto = ManagerDTO.builder()
                .id(manager.getManagerId())
                .ativo(manager.isActive())
                .cpf(manager.getCpf())
                .email(manager.getEmail())
                .nome(manager.getName())
                .telefone(manager.getPhone())
                .build();

        managerRepository.save(manager);

        return managerDto;
    }

    public List<String> listManagerCPFs() {
        return this.managerRepository.findCPFs();
    }

    public List<ManagerDTO> list() {
        List<Manager> managers = managerRepository.findAllByIsActiveOrderByNameAsc(true);
        Iterator<Manager> managerIterator = managers.iterator();

        List<ManagerDTO> managerList = new ArrayList<>();

        while (managerIterator.hasNext()) {
            Manager manager = managerIterator.next();
            ManagerDTO dto = ManagerDTO.builder()
                    .id(manager.getManagerId())
                    .ativo(manager.isActive())
                    .cpf(manager.getCpf())
                    .email(manager.getEmail())
                    .nome(manager.getName())
                    .telefone(manager.getPhone())
                    .build();

            managerList.add(dto);
        }

        return managerList;
    }

    public ManagerDTO getManager(String cpf) {
        Manager manager = managerRepository.findByCpfAndIsActive(cpf, true);

        return ManagerDTO.builder()
                .id(manager.getManagerId())
                .ativo(manager.isActive())
                .cpf(manager.getCpf())
                .email(manager.getEmail())
                .nome(manager.getName())
                .telefone(manager.getPhone())
                .build();
    }

    public ManagerDTO updateManager(String managerCPF, UpdateManagerDTO updateManagerDTO) {
        if (updateManagerDTO.getEmail() != null || updateManagerDTO.getCpf() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail e CPF não podem ser alterados");
        }

        ManagerDTO managerDto = this.getManager(managerCPF);

        managerDto.setNome(updateManagerDTO.getNome());
        managerDto.setTelefone(updateManagerDTO.getTelefone());
        managerDto.setAtivo(updateManagerDTO.getAtivo());

        Manager manager = new Manager();
        manager.setManagerId(managerDto.getId());
        manager.setName(managerDto.getNome());
        manager.setPhone(managerDto.getTelefone());
        manager.setActive(managerDto.getAtivo());

        managerRepository.save(manager);

        return ManagerDTO.builder()
                .id(manager.getManagerId())
                .ativo(manager.isActive())
                .cpf(manager.getCpf())
                .email(manager.getEmail())
                .nome(manager.getName())
                .telefone(manager.getPhone())
                .build();
    }
    
    public void deleteByCpf(String cpf) {
        this.managerRepository.deleteByCpf(cpf);
    }
    
    // --- MANAGER SAGA --
    
    @RabbitListener(queues = RabbitMQConfig.MANAGER_QUEUE)
    public void handleManagerQueue(RabbitRequest command) {
        
        Status status = Status.SUCESSO;
        String error = null;
        Map<String, Object> payload = new HashMap<>();
        
        Map<String, Object> reply = new HashMap<>();
        reply.put("sagaId", command.getSagaId());
        reply.put("type", command.getType());
        reply.put("stepIndex", command.getStepIndex());
        reply.put("attempt", command.getAttempt());
        reply.put("payload", payload);

        try {

            boolean alreadyProcessed = requestRepository.existsBySagaIdAndType(command.getSagaId(), command.getType());

            if (alreadyProcessed) {
                this.sendReadModelCommand(reply);
                return;
            }

            String action = command.getType();

            if (!this.handlers.containsKey(action)) {
                throw new RuntimeException("Handler não encontrado para a ação " + action);
            }

            SagaStepHandler handler = this.handlers.get(action);
            
            if (command.isCompensation()) {
                handler.compensate(command.getPayload());
            } else {
                payload = handler.execute(command.getPayload());
            }

            requestRepository.save(Request.builder()
                    .sagaId(command.getSagaId())
                    .type((command.isCompensation() ? command.getType() + ".comp" : command.getType())) // comp para sinalizar um registro de compensacao no banco
                    .build());
            
        } catch (Exception e) {
            status = Status.FALHA;
            error = e.getMessage();
            e.printStackTrace();
        }


        reply.put("status", status.toString());
        reply.put("error", error);

        this.sendReadModelCommand(reply);
    }

    // -- COMPENSACAO --
    @RabbitListener(queues = RabbitMQConfig.MANAGER_QUEUE_DLQ)
    public void handleDlqQueue(Map<String, Object> payload) {
        UUID sagaId = UUID.fromString(payload.get("sagaId").toString());
        String type = payload.get("type").toString();

        if (requestRepository.existsBySagaIdAndType(sagaId, type + ".comp")) {
            return;
        }

        SagaStepHandler handler = this.handlers.get(type);

        if (handler != null) {
            handler.compensate(payload);
        }

        requestRepository.save(Request.builder()
                .sagaId(sagaId)
                .type(type + ".comp")
                .build());
    }

    public void sendReadModelCommand(Map<String, Object> answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
