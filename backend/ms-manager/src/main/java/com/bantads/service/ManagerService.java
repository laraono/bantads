package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.CreateManagerDTO;
import com.bantads.dto.ManagerDTO;
import com.bantads.dto.UpdateManagerDTO;
import com.bantads.entity.Manager;
import com.bantads.entity.Request;
import com.bantads.model.RabbitAnswer;
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

@Service
public class ManagerService {

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public Manager createManager(CreateManagerDTO createManagerDTO) {
        Manager manager = Manager.builder()
                .cpf(createManagerDTO.getCpf())
                .email(createManagerDTO.getEmail())
                .name(createManagerDTO.getNome())
                .phone(createManagerDTO.getTelefone())
                .build();

        return managerRepository.save(manager);
    }

    public Map<String, Object> listManagers() {
        List<Manager> managers = managerRepository.findAllByIsActiveOrderByNameAsc(true);
        Iterator<Manager> managerIterator = managers.iterator();

        List<ManagerDTO> managerList = new ArrayList<>();

        while(managerIterator.hasNext()) {
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

        Map<String, Object> gerentes = new HashMap<>();
        gerentes.put("gerentes", managerList);

        return gerentes;
    }

    public List<ManagerDTO> list() {
        List<Manager> managers = managerRepository.findAllByIsActiveOrderByNameAsc(true);
        Iterator<Manager> managerIterator = managers.iterator();

        List<ManagerDTO> managerList = new ArrayList<>();

        while(managerIterator.hasNext()) {
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

    public Manager getManager(String cpf) {
        return managerRepository.findByCpfAndIsActive(cpf, true);
    }

    public void deleteManager(String managerCPF, String userCPF) {
         Manager manager = this.getManager(managerCPF);

        if(manager.getCpf().equals(userCPF)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível deletar a si mesmo");
        }

        manager.setActive(false);

        managerRepository.save(manager);

    }

    public ManagerDTO updateManager(String managerCPF, UpdateManagerDTO updateManagerDTO) {
        if (updateManagerDTO.getEmail() != null || updateManagerDTO.getCpf() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail e CPF não podem ser alterados");
        }

        Manager manager = this.getManager(managerCPF);

        manager.setName(updateManagerDTO.getNome());
        manager.setPhone(updateManagerDTO.getTelefone());

        Manager salvo = managerRepository.save(manager);

        return ManagerDTO.builder()
                .id(salvo.getManagerId())
                .ativo(salvo.isActive())
                .cpf(salvo.getCpf())
                .email(salvo.getEmail())
                .nome(salvo.getName())
                .telefone(salvo.getPhone())
                .build();
    }

    @RabbitListener(queues = RabbitMQConfig.MANAGER_QUEUE)
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

        if(type.contains("listar-gerentes")) {
            try {
                List<ManagerDTO> gerentes = this.list();
                payload.put("gerentes", gerentes);

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
