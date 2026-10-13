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
import java.util.*;

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

    public List<String> getAllManagersCpf() {
        return this.accountDataRepository.findAllUniqueManagerCPF();
    }

    public String getManagerWithLowestAccount(List<String> managerCPFs) {
        String best = null;
        long lowestAccountAmount = Long.MAX_VALUE;
        BigDecimal lowestTotalBalance = BigDecimal.ZERO;

        for (String cpf : managerCPFs) {
            AccountsByManagerDTO current = this.getAccountsCountByManager(cpf);
            long amount = current == null ? 0 : current.getTotalAccounts();
            BigDecimal balance = current == null || current.getTotalBalance() == null ? BigDecimal.ZERO : current.getTotalBalance();

            if (best == null
                    || amount < lowestAccountAmount
                    || (amount == lowestAccountAmount && balance.compareTo(lowestTotalBalance) < 0)) {
                best = cpf;
                lowestAccountAmount = amount;
                lowestTotalBalance = balance;
            }
        }

        return best;
    }

    public Map<String, Object> getAccountToNewManager(Map<String, Object> payload ) {
        List<String> cpfs = this.accountDataRepository.findManagersWithMultipleAccountsOrderedByCount();

        if(cpfs.isEmpty()) {
            payload.put("semConta", "");
            return payload;
        }

        String managerCpf = cpfs.getFirst();

        List<AccountData> accounts = this.accountDataRepository.findAllByManagerCPFOrderByBalanceAsc(managerCpf);

        AccountData chosenAccount = accounts.getFirst();

        payload.put("numeroConta", chosenAccount.getAccountNumber());
        payload.put("cpfCliente", chosenAccount.getClientCPF());
        payload.put("gerenteAntigo", managerCpf);

        return payload;
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

    public void deleteAccountData(String accountNumber) {
        AccountData account = this.accountDataRepository.findByAccountNumber(accountNumber);

        if (account == null) {
            return;
        }

        account.setDeletedAt(new java.util.Date());
        this.accountDataRepository.save(account);
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

    public List<String> findManagersWithMultipleAccounts() {
        return this.accountDataRepository.findManagersWithMultipleAccountsOrderedByCount();
    }

}
