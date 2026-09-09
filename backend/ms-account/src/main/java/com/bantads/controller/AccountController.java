package com.bantads.controller;

import com.bantads.dto.event.GetAccountDTO;
import com.bantads.dto.read.AccountsByManagerDTO;
import com.bantads.dto.read.ExtractDTO;
import com.bantads.dto.read.RabbitQueryDTO;
import com.bantads.entity.read.AccountData;
import com.bantads.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/{accountNumber}")
    ResponseEntity<GetAccountDTO> getAccountData(@PathVariable String accountNumber) {
        GetAccountDTO account = this.accountService.getAccountData(accountNumber);

        return ResponseEntity.status(HttpStatus.OK).body(account);
    }

    @GetMapping("/{accountNumber}/extract")
    ResponseEntity<ExtractDTO> getExtract(
            @RequestHeader("x-user-cpf") String userCPF,
            @PathVariable String accountNumber,
            @RequestParam(value = "inicio", required = false) String start,
            @RequestParam(value = "fim", required = false) String end
    ) {
        ExtractDTO extract = this.accountService.getExtract(accountNumber, start, end, userCPF);

        return ResponseEntity.status(HttpStatus.OK).body(extract);
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
