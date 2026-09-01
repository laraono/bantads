package com.bantads.service;

import com.bantads.dto.read.AccountsByManagerDTO;
import com.bantads.entity.read.AccountData;
import com.bantads.error.BadRequestError;
import com.bantads.repository.read.AccountDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class AccountDataService {

    @Autowired
    private AccountDataRepository accountDataRepository;

    public String getCPF(String accountNumber) {
        AccountData account = this.accountDataRepository.getReferenceById(accountNumber);

        return account.getClientCPF();
    }

    public BigDecimal getBalance(String accountNumber) {
        AccountData account = this.accountDataRepository.getReferenceById(accountNumber);

        return account.getBalance();
    }

    public List<AccountData> getAccountsByManager(String managerCPF) {
        return this.accountDataRepository.findAllByManagerCPF(managerCPF);
    }

    public List<AccountsByManagerDTO> getAccountsCountByManager() {
        return this.accountDataRepository.getAccountCountByManager();
    }

    public AccountData getAccountData(String accountNumber) {
        return this.accountDataRepository.getReferenceById(accountNumber);
    }

    public AccountData createAccountData(AccountData accountData) {
        if(this.accountDataRepository.existsAccountDataByAccountNumber(accountData.getAccountNumber())) {
            throw new BadRequestError("account already exists");
        }

        return this.accountDataRepository.save(accountData);
    }

    public AccountData updateAccountBalance(BigDecimal value, String accountNumber) {
        AccountData account = this.accountDataRepository.getReferenceById(accountNumber);

        BigDecimal balance = account.getBalance();

        account.setBalance(balance.add(value));

        return this.accountDataRepository.save(account);
    }

    public AccountData updateAccountManager(String managerCPF, String accountNumber) {
        AccountData account = this.accountDataRepository.getReferenceById(accountNumber);

        account.setManagerCPF(managerCPF);

        return this.accountDataRepository.save(account);
    }


}
