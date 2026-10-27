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
        String cpf = String.valueOf(payload.get("idSolicitacao"));
        this.requestService.approveRequest(cpf);
        Request request = this.requestService.getRequestByCpf(cpf);
        payload.put("cpfCliente", request.getCpf());
        payload.put("requisicao", request);
        return payload;

    }

    @Override
    public void compensate(Map<String, Object> payload, String error) {
        String id = (String) payload.get("idSolicitacao");
        if ("CONTA JÁ EXISTE".equals(error)) {
            this.requestService.rejectRequest(id, error);
        } else {
            this.requestService.compensateRequestStatus(id);
        }

        return;
    }

}
