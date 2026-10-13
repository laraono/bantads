package com.bantads.handlers;

import java.util.Map;

import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import com.bantads.entity.Client;
import com.bantads.service.ClientService;

@Component
public class CreateClientHandler implements SagaStepHandler {

    @Autowired 
    private ClientService clientService;
    
    @Override
    public String action() {
        return "cliente.criar-cliente";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        Client client = clientService.createClient(payload);

        payload.put("cpfCliente", client.getCpf());
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        clientService.deleteClient(payload);
        return;
    }
    
}
