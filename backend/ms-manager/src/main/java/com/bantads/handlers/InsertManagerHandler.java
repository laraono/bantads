package com.bantads.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.bantads.dto.CreateManagerDTO;
import com.bantads.dto.ManagerDTO;
import com.bantads.service.ManagerService;

@Component
public class InsertManagerHandler implements SagaStepHandler {

    @Autowired
    @Lazy
    private ManagerService managerService;

    @Autowired
    private Validator validator;

    @Override
    public String action() {
        return "gerente.inserir-gerente";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> payload) {
        Map<String, Object> dados = (Map<String, Object>) payload.get("gerente");
        if(dados == null) {
            throw new IllegalArgumentException("Dados não informados");
        }
        
        CreateManagerDTO managerDTO = CreateManagerDTO.builder()
                .nome(asString(dados.get("nome")))
                .email(asString(dados.get("email")))
                .cpf(asString(dados.get("cpf")))
                .telefone(asString(dados.get("telefone")))
                .senha(asString(dados.get("senha")))
                .build();

        Set<ConstraintViolation<CreateManagerDTO>> violacoes = validator.validate(managerDTO);
        if (!violacoes.isEmpty()) {
            throw new IllegalArgumentException(violacoes.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining("; ")));
        }

        ManagerDTO manager = this.managerService.createManager(managerDTO);

        Map<String, Object> gerente = new HashMap<>();
        gerente.put("cpf", manager.getCpf());
        gerente.put("email", manager.getEmail());
        gerente.put("nome", manager.getNome());
        gerente.put("telefone", manager.getTelefone());

        Map<String, String> requisicao = new HashMap<>();
        requisicao.put("email", manager.getEmail());
        requisicao.put("id", String.valueOf(manager.getId()));
        requisicao.put("cpf", manager.getCpf());
        requisicao.put("tipo", "GERENTE");
        requisicao.put("id", manager.getCpf());
        requisicao.put("senha", managerDTO.getSenha());

        payload.put("gerente", gerente);
        payload.put("requisicao", requisicao);
        return payload;
    }

    private static String asString(Object valor) {
        return valor == null ? null : String.valueOf(valor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void compensate(Map<String, Object> payload) {
        Map<String, Object> gerente = (Map<String, Object>) payload.get("gerente");
        if (gerente == null) {
            return;
        }

        String cpf = String.valueOf(((Map<String, Object>) gerente).get("cpf"));
        this.managerService.deleteByCpf(cpf);
    }

}
