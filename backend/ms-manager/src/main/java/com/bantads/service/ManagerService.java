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
import org.springframework.transaction.annotation.Transactional;
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

        if (managerRepository.existsByEmail(createManagerDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        if (managerRepository.existsByCpf(createManagerDTO.getCpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }

        Manager manager = Manager.builder()
                .cpf(createManagerDTO.getCpf())
                .email(createManagerDTO.getEmail())
                .name(createManagerDTO.getNome())
                .phone(createManagerDTO.getTelefone())
                .isActive(true)
                .build();

        Manager managerSaved = managerRepository.save(manager); // obtém o id

        ManagerDTO managerDto = ManagerDTO.builder()
                .id(managerSaved.getManagerId())
                .ativo(managerSaved.isActive())
                .cpf(managerSaved.getCpf())
                .email(managerSaved.getEmail())
                .nome(managerSaved.getName())
                .telefone(managerSaved.getPhone())
                .build();

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

        ManagerDTO managerDto = this.getManager(managerCPF);

        if (updateManagerDTO.getNome() != null)
            managerDto.setNome(updateManagerDTO.getNome());

        if (updateManagerDTO.getTelefone() != null)
            managerDto.setTelefone(updateManagerDTO.getTelefone());

        Manager manager = new Manager();
        manager.setManagerId(managerDto.getId());
        manager.setName(managerDto.getNome());
        manager.setPhone(managerDto.getTelefone());
        manager.setCpf(managerDto.getCpf());
        manager.setEmail(managerDto.getEmail());
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

    @Transactional
    public void deleteByCpf(String cpf) {
        this.managerRepository.deleteByCpf(cpf);
    }

    @RabbitListener(queues = RabbitMQConfig.MANAGER_QUEUE)
    public void handleManagerQueue(RabbitRequest command) {

        Status status = Status.SUCESSO;
        String error = null;

        Map<String, Object> payload = command.getPayload();

        String action = command.getType();
        SagaStepHandler handler = this.handlers.get(action);

        if (handler == null) {
            throw new RuntimeException("Handler não encontrado para a ação " + action);
        }

        try {

            // idempotência do step
            boolean alreadyProcessed = requestRepository.existsBySagaIdAndType(command.getSagaId(),
                    command.isCompensation() ? command.getType() + ".comp" : command.getType());

            if (alreadyProcessed) {
                this.sendReadModelCommand(command);
                return;
            }
           
            payload = handler.execute(command.getPayload());


            requestRepository.save(Request.builder()
                    .sagaId(command.getSagaId())
                    .type(command.getType())
                    .build());

        } catch (Exception e) {
            status = Status.FALHA;
            error = (e instanceof ResponseStatusException rse && rse.getReason() != null) ? rse.getReason()
                    : e.getMessage();
            e.printStackTrace();
        }

        command.setPayload(payload);
        command.setStatus(status.toString());
        command.setError(error);
        command.setTimestamp(new Date());

        this.sendReadModelCommand(command);
    }

    @RabbitListener(queues = RabbitMQConfig.MANAGER_QUEUE_DLQ)
    public void handleDlqQueue(RabbitRequest command) {
        UUID sagaId = command.getSagaId();
        String type = command.getType();

        if (requestRepository.existsBySagaIdAndType(sagaId, type + ".comp")) {
            return;
        }

        SagaStepHandler handler = this.handlers.get(type);

        Map<String, Object> payload = command.getPayload();
        
        Request marker = requestRepository.save(Request.builder()
        // é útil pra sinalizar o quanto antes que uma compensação está acontecendo
        // para uma msg válida
                .sagaId(sagaId)
                .type(type + ".comp")
                .build());

        try {
            if (handler != null) {
                handler.compensate(payload);
            }
        } catch (RuntimeException e) {
            requestRepository.delete(marker); // se der erro ainda dá pra refazer a compensação
            throw e;
        }

    }

    public void sendReadModelCommand(RabbitRequest answer) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORQUESTRADOR_QUEUE, answer);
    }
}
