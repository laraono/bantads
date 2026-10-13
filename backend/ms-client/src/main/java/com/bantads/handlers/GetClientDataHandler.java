package com.bantads.handlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bantads.entity.Client;
import com.bantads.service.ClientService;

@Component
public class GetClientDataHandler implements SagaStepHandler {

    @Autowired
    private ClientService clientService;

    @Override
    public String action() {
        return "cliente.obter-dados";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        String cpf = String.valueOf(payload.get("cpfCliente"));
        Client client = this.clientService.getClient(cpf);

        payload.put("nome", client.getName());
        payload.put("email", client.getEmail());
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        return;
    }

}
