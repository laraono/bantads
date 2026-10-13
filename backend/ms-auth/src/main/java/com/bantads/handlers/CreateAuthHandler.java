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
        Map<String, String> request = (Map<String, String>) payload.get("requisicao");
        String password = this.authService.create(
                request.get("id"),
                request.get("cpf"),
                request.get("tipo"),
                request.get("email"),
                request.get("senha"),
                true);
        Map<String, String> email = new HashMap<>();

        email.put("email", request.get("email"));
        email.put("senha", password);

        payload.put("email", email);
        
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        Map<String, String> request = (Map<String, String>) payload.get("requisicao");

        this.authService.invalidateAuth(request.get("cpf"));
        return;
    }

}
