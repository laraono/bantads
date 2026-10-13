package com.bantads.handlers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bantads.model.Status;
import com.bantads.service.EventService;

@Component
public class CreateAccountHandler implements SagaStepHandler {

    @Autowired
    EventService eventService;
    
    @Override
    public String action() { return "conta.criar-conta"; }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        String cpfGerente = String.valueOf(payload.get("cpfGerente"));
        String cpfCliente = String.valueOf(payload.get("cpfCliente"));

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("cpfGerente", cpfGerente);
        eventPayload.put("cpfCliente", cpfCliente);

        this.eventService.createAccount(eventPayload);
         
        return Map.of();
    }
    

    @Override
    public void compensate(Map<String, Object> payload) {
        this.eventService.deleteAccount(payload);
        return;
    }

}
