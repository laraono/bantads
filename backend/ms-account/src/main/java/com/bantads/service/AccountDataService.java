package com.bantads.service;

import com.bantads.dto.event.GetAccountDTO;
import com.bantads.dto.read.AccountsByManagerDTO;
import com.bantads.entity.read.AccountData;
import com.bantads.repository.read.AccountDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class AccountDataService {

    @Autowired
    private AccountDataRepository accountDataRepository;

    public String getCPF(String accountNumber) {
        if(!this.accountDataRepository.existsAccountDataByAccountNumber(accountNumber)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Conta " + accountNumber + " não existe");
        }

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

    public AccountsByManagerDTO getAccountsCountByManager(String cpf) {
        return this.accountDataRepository.getAccountCountByManager(cpf);
    }

    public String getManagerWithLowestAccount(List<String> managerCPFs) {

        Iterator<String> cpfIterator = managerCPFs.iterator();
        String firstCPF = managerCPFs.get(0);
        AccountsByManagerDTO first = this.getAccountsCountByManager(firstCPF);

        int lowestAccountAmount = first.getTotalAccounts();
        BigDecimal lowestTotalBalance = first.getTotalBalance();
        int index = 0;
        int i = 1;

        while(cpfIterator.hasNext()) {
            AccountsByManagerDTO current = this.getAccountsCountByManager(cpfIterator.next());
            if (current.getTotalAccounts() < lowestAccountAmount) {
                lowestAccountAmount = current.getTotalAccounts();
                lowestTotalBalance = current.getTotalBalance();
                index = i;
            } else if (current.getTotalAccounts() == lowestAccountAmount && current.getTotalBalance().compareTo(lowestTotalBalance) < 0) {
                lowestAccountAmount = current.getTotalAccounts();
                lowestTotalBalance = current.getTotalBalance();
                index = i;
            }
            i++;
        }

        return managerCPFs.get(index);
    }


    public AccountData getAccountData(String accountNumber) {
        return this.accountDataRepository.findByAccountNumber(accountNumber);
    }

    public AccountData createAccountData(AccountData accountData) {
        if(this.accountDataRepository.existsAccountDataByAccountNumber(accountData.getAccountNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Conta já existe");
        }

        return this.accountDataRepository.save(accountData);
    }

    public AccountData updateAccountBalance(BigDecimal value, String accountNumber) {
        if(!this.accountDataRepository.existsAccountDataByAccountNumber(accountNumber)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Conta " + accountNumber + " não existe");
        }

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

    public List<AccountData> listAccounts(List<String> clientCPFs) {
        if (clientCPFs == null || clientCPFs.isEmpty()) {
            return this.accountDataRepository.findAll();
        }
        return this.accountDataRepository.findAllByClientCPFIn(clientCPFs);
    }

    public AccountData getAccountDataByCpf(String cpf) {
        return this.accountDataRepository.findByClientCPF(cpf);
    }

    public void deleteAccount(String cpf) {
        this.accountDataRepository.deleteByClientCPF(cpf);
    }

}
