package com.bantads.handlers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bantads.entity.Client;
import com.bantads.service.ClientService;

@Component
public class GetClientsEmailsHandler implements SagaStepHandler {

    @Autowired
    private ClientService clientService;

    @Override
    public String action() {
        return "cliente.obter-email-clientes";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        List<String> clientesCPF = (List<String>) payload.get("clientesCPF");
        List<Client> clientes = this.clientService.getClientsByCpf(clientesCPF);
        List<String> emails = clientes.stream().map(Client::getEmail).toList();
        payload.put("emails", emails);
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        return;
    }

}