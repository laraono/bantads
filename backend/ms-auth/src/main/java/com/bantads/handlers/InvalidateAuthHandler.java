package com.bantads.handlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bantads.service.AuthService;

@Component
public class InvalidateAuthHandler implements SagaStepHandler {

    @Autowired
    AuthService authService;

    @Override
    public String action() {
        return "auth.deletar-gerente";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        this.authService.invalidateAuth(payload.get("cpfGerente").toString());
        return Map.of();
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        return;
    }

}