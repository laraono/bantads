package com.bantads.handlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.bantads.entity.Manager;
import com.bantads.repository.ManagerRepository;

@Component
public class DeleteManagerHandler implements SagaStepHandler {

    @Autowired
    private ManagerRepository managerRepository;

    @Override
    public String action() {
        return "gerente.deletar-gerente";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        Manager manager = this.managerRepository.findByCpfAndIsActive(String.valueOf(payload.get("cpfGerente")), true);

        if (manager == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gerente não encontrado");
        }
        
        manager.setActive(false);
        this.managerRepository.save(manager);
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        Manager manager = this.managerRepository.findByCpf(String.valueOf(payload.get("cpfGerente")));

        if (manager == null) {
            return;
        }

        manager.setActive(true);
        this.managerRepository.save(manager);
    }

}
