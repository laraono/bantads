package com.bantads.handlers;

import java.util.Map;

import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import com.bantads.entity.Client;
import com.bantads.service.ClientService;

@Component
public class CreateClientHandler implements SagaStepHandler {

    // private static final java.util.concurrent.atomic.AtomicInteger falhas = new java.util.concurrent.atomic.AtomicInteger();

    @Autowired 
    private ClientService clientService;
    
    @Override
    public String action() {
        return "cliente.criar-cliente";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
    //    if (falhas.getAndIncrement() % 3 < 2) throw new RuntimeException("\n \n =========== Attempt: "+ falhas + " em " + action() + " ========== \n \n");
        
        Client client = clientService.createClient(payload);

        payload.put("cpfCliente", client.getCpf());
        payload.put("tipo", "CLIENTE");
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        clientService.deleteClient(payload);
        return;
    }
    
}
