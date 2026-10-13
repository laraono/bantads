package com.bantads.handlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bantads.entity.Request;
import com.bantads.service.RequestService;

@Component
public class ApproveRequestHandler implements SagaStepHandler {

    @Autowired
    private RequestService requestService;

    @Override
    public String action() {
        return "cliente.aprovar-solicitacao";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        Long id = (Long) payload.get("idSolicitacao");
        this.requestService.approveRequest(id);
        Request request = this.requestService.getRequestById(id);
        payload.put("cpfCliente", request.getCpf());
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        return;
    }

}
