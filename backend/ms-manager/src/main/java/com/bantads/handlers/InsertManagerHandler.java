package com.bantads.handlers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.bantads.dto.CreateManagerDTO;
import com.bantads.entity.Manager;
import com.bantads.service.ManagerService;

@Component
public class InsertManagerHandler implements SagaStepHandler {

    @Autowired
    @Lazy
    private ManagerService managerService;

    @Override
    public String action() {
        return "gerente.inserir-gerente";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> payload) {
        Map<String, Object> dados = (Map<String, Object>) payload.get("gerente");
        CreateManagerDTO managerDTO = CreateManagerDTO.builder()
                .nome(String.valueOf(dados.get("nome")))
                .email(String.valueOf(dados.get("email")))
                .cpf(String.valueOf(dados.get("cpf")))
                .telefone(String.valueOf(dados.get("telefone")))
                .senha(String.valueOf(dados.get("senha")))
                .build();
        Manager manager = this.managerService.createManager(managerDTO);

        Map<String, Object> gerente = new HashMap<>();
        gerente.put("cpf", manager.getCpf());
        gerente.put("email", manager.getEmail());
        gerente.put("nome", manager.getName());
        gerente.put("telefone", manager.getPhone());

        Map<String, String> requisicao = new HashMap<>();
        requisicao.put("email", manager.getEmail());
        requisicao.put("cpf", manager.getCpf());
        requisicao.put("tipo", "GERENTE");
        requisicao.put("id", manager.getCpf());
        requisicao.put("senha", managerDTO.getSenha());

        payload.put("gerente", gerente);
        payload.put("requisicao", requisicao);
        return payload;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void compensate(Map<String, Object> payload) {
        Object gerente = payload.get("gerente");
        if (gerente == null) {
            return;
        }

        String cpf = String.valueOf(((Map<String, Object>) gerente).get("cpf"));
        this.managerService.deleteByCpf(cpf);
    }

}
