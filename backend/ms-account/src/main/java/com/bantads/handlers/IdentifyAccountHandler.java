package com.bantads.handlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bantads.service.AccountService;

@Component
public class IdentifyAccountHandler implements SagaStepHandler {

    @Autowired
    private AccountService accountService;

    @Override
    public String action() {
        return "conta.identificar-conta";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        return this.accountService.getAccountToNewManager(payload);
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        return;
    }

}
