package com.bantads.handlers;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bantads.model.Status;
import com.bantads.service.AccountService;

import jakarta.persistence.Access;

@Component
public class DecideManagerHandler implements SagaStepHandler {

    @Autowired
    private AccountService accountService;
    
    @Override
    public String action() {
        return "conta.decidir-gerente";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        String cpf = this.accountService.findManager(payload);
        payload.put("cpfGerente", cpf);
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        return;
    }

}
