package com.bantads.service;

import com.bantads.dto.read.AccountsByManagerDTO;
import com.bantads.entity.read.AccountData;
import com.bantads.repository.read.AccountDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class AccountDataService {

    @Autowired
    private AccountDataRepository accountDataRepository;

    public String getCPF(String accountNumber) {
        AccountData account = this.accountDataRepository.findByAccountNumber(accountNumber);

        return account.getClientCPF();
    }

    public BigDecimal getBalance(String accountNumber) {
        AccountData account = this.accountDataRepository.findByAccountNumber(accountNumber);

        return account.getBalance();
    }

    public List<AccountData> getAccountsByManager(String managerCPF) {
        return this.accountDataRepository.findAllByManagerCPF(managerCPF);
    }

    public List<AccountsByManagerDTO> getAccountsCountByManager() {
        return this.accountDataRepository.getAccountCountByManager();
    }

    public AccountData getAccountData(String accountNumber) {
        return this.accountDataRepository.findByAccountNumber(accountNumber);
    }

    public AccountData createAccountData(AccountData accountData) {
        if(this.accountDataRepository.existsAccountDataByAccountNumber(accountData.getAccountNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "account already exists");
        }

        return this.accountDataRepository.save(accountData);
    }

    public AccountData updateAccountBalance(BigDecimal value, String accountNumber) {
        AccountData account = this.accountDataRepository.findByAccountNumber(accountNumber);

        BigDecimal balance = account.getBalance();

        account.setBalance(balance.add(value));

        return this.accountDataRepository.save(account);
    }

    public AccountData updateAccountManager(String managerCPF, String accountNumber) {
        AccountData account = this.accountDataRepository.findByAccountNumber(accountNumber);

        account.setManagerCPF(managerCPF);

        return this.accountDataRepository.save(account);
    }


}
