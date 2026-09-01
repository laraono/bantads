package com.bantads.controller;

import com.bantads.dto.read.AccountsByManagerDTO;
import com.bantads.dto.read.RabbitQueryDTO;
import com.bantads.entity.read.AccountData;
import com.bantads.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin
@RestController("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/{accountNumber}")
    AccountData getAccountData(@PathVariable String accountNumber) {
        return this.accountService.getAccountData(accountNumber);
    }

    @GetMapping("/{accountNumber}/cpf")
    String getCPF(@PathVariable String accountNumber) {
        return this.accountService.getCPF(accountNumber);
    }

    @GetMapping("/{accountNumber}/balance")
    BigDecimal getBalance(@PathVariable String accountNumber) {
        return this.accountService.getBalance(accountNumber);
    }

    @GetMapping("/managers/{cpf}/")
    List<AccountData> getAccountsByManager(@PathVariable String cpf) {
        return this.accountService.getAccountsByManager(cpf);
    }

    @GetMapping("/managers")
    List<AccountsByManagerDTO> getAccountsCount() {
        return this.accountService.getAccountsCountByManager();
    }

}
