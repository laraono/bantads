package com.bantads.service;

import com.bantads.dto.CreateManagerDTO;
import com.bantads.dto.ManagerDTO;
import com.bantads.dto.UpdateManagerDTO;
import com.bantads.entity.Manager;
import com.bantads.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ManagerService {

    @Autowired
    private ManagerRepository managerRepository;

    public Manager createManager(CreateManagerDTO createManagerDTO) {
        Manager manager = Manager.builder()
                .cpf(createManagerDTO.getCpf())
                .email(createManagerDTO.getEmail())
                .name(createManagerDTO.getNome())
                .phone(createManagerDTO.getTelefone())
                .build();

        return managerRepository.save(manager);
    }

    public List<ManagerDTO> listManagers() {
        List<Manager> managers = managerRepository.findAllByIsActive(true);
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

    public Manager getManager(Long id) {
        return managerRepository.findDistinctByManagerIdAndIsActive(id, true);
    }

    public void deleteManager(Long id, String userCPF) {
        List<Manager> managers = managerRepository.findByManagerIdNotAndIsActive(id, true);

        if(!managers.isEmpty()) {
            Manager manager = this.getManager(id);

            if(manager.getCpf().equals(userCPF)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível deletar a si mesmo");
            }

            manager.setActive(false);

            managerRepository.save(manager);
        }
    }

    public Manager getManagerByCpf(String cpf) {
        return managerRepository.findByCpf(cpf);
    }

    public void updateManager(Long id, UpdateManagerDTO updateManagerDTO) {
        Manager manager = this.getManager(id);

        manager.setName(updateManagerDTO.getNome());
        manager.setPhone(updateManagerDTO.getTelefone());

        managerRepository.save(manager);
    }

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
