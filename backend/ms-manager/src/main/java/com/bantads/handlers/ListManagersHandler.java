package com.bantads.handlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.bantads.service.ManagerService;

@Component
public class ListManagersHandler implements SagaStepHandler {

    @Autowired
    @Lazy
    private ManagerService managerService;

    @Override
    public String action() {
        return "gerente.listar-gerentes";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        payload.put("gerentes", this.managerService.listManagerCPFs());
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        return;
    }

}
