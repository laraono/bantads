package com.bantads.handlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bantads.service.EventService;

@Component
public class AssociateManagerHandler implements SagaStepHandler {

    @Autowired
    private EventService eventService;

    @Override
    public String action() {
        return "conta.associar-gerente";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> payload) {
        if (payload.containsKey("semConta")) {
            return payload;
        }

        Map<String, Object> gerente = (Map<String, Object>) payload.get("gerente");
        String managerCpf = String.valueOf(gerente.get("cpf"));
        String accountNumber = String.valueOf(payload.get("numeroConta"));

        this.eventService.moveAccountToManager(accountNumber, managerCpf);
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        if (payload.containsKey("semConta") || payload.get("gerenteAntigo") == null) {
            return;
        }

        String oldManager = String.valueOf(payload.get("gerenteAntigo"));
        String accountNumber = String.valueOf(payload.get("numeroConta"));

        this.eventService.moveAccountToManager(accountNumber, oldManager);
    }

}
