package com.bantads.handlers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bantads.service.AuthService;

@Component
public class CreateAuthHandler implements SagaStepHandler {

    @Autowired 
    private AuthService authService;

    @Override
    public String action() {
        return "auth.criar-auth";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        Map<String, Object> request = (Map<String, Object>) payload.get("requisicao");
        String id = request.get("id") != null ? String.valueOf(request.get("id")) : null;
        Object tipo = request.getOrDefault("tipo", payload.get("tipo"));
        String password = this.authService.create(
                id,
                String.valueOf(request.get("cpf")),
                String.valueOf(tipo),
                String.valueOf(request.get("email")),
                String.valueOf(request.getOrDefault("senha", "")),
                true);
        Map<String, String> email = new HashMap<>();

        email.put("email", String.valueOf(request.get("email")));
        email.put("senha", password);

        payload.put("email", email);
        
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        Map<String, Object> request = (Map<String, Object>) payload.get("requisicao");

        this.authService.invalidateAuth(String.valueOf(request.get("cpf")));
        return;
    }

}
