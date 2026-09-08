package com.bantads.service;

import com.bantads.dto.read.AccountHistoryDTO;
import com.bantads.entity.read.AccountHistory;
import com.bantads.repository.read.AccountHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AccountHistoryService {

    @Autowired
    private AccountHistoryRepository accountHistoryRepository;

    public AccountHistory createAcountHistory(AccountHistoryDTO ac) {
        AccountHistory accountHistory = AccountHistory.builder()
            .accountNumber(ac.getAccountNumber())
            .amount(ac.getAmount())
            .createdAt(new Date())
            .destinationClientCPF(ac.getDestinationClientCPF())
            .destinationClientName(ac.getDestinationClientName())
            .managerCPF(ac.getManagerCPF())
            .managerName(ac.getManagerName())
            .originClientCPF(ac.getOriginClientCPF())
            .originClientName(ac.getOriginClientName())
            .type(ac.getType())
            .build();

        return accountHistoryRepository.save(accountHistory);
    }

    public List<AccountHistory> getAccountHistoryByAccountNumber(String accountNumber) {
        return this.accountHistoryRepository.findAllByAccountNumber(accountNumber);
    }
}
