package com.bantads.handlers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bantads.entity.read.AccountData;
import com.bantads.service.AccountDataService;
import com.bantads.service.EventService;

@Component 
public class RemoveManagerHandler implements SagaStepHandler {

    @Autowired 
    private EventService eventService;

    @Autowired
    private AccountDataService accountDataService;

    @Override
    public String action() {
        return "conta.deletar-gerente";
    }
    
    @Override
    public Map<String, Object> execute(Map<String, Object> payload) {
        List<String> response = this.eventService.deleteManager(payload);
        payload.put("contasMovidas", response); 
        // o MS Cliente busca os e-mails pelos CPFs dos donos das contas transferidas
        List<String> clientesCPF = response.stream()
                .map(this.accountDataService::getAccountData)
                .filter(java.util.Objects::nonNull)
                .map(AccountData::getClientCPF)
                .toList();
        payload.put("clientesCPF", clientesCPF);
        return payload;
    }

    @Override
    public void compensate(Map<String, Object> payload) {
        String oldManager = String.valueOf(payload.get("cpfGerente"));
        List<String> movedAccounts = (List<String>) payload.get("contasMovidas");

        if (movedAccounts == null) {
            return;
        }

        for (String accountNumber : movedAccounts) {
            this.eventService.moveAccountToManager(accountNumber, oldManager);
        }
    }

}
