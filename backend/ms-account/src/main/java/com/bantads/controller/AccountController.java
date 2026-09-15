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
        try {
            GetAccountDTO account = this.accountService.getAccountData(accountNumber);
            return ResponseEntity.status(HttpStatus.OK).body(account);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{accountNumber}/extract")
    ResponseEntity<ExtractDTO> getExtract(
            @RequestHeader("x-user-cpf") String userCPF,
            @PathVariable String accountNumber,
            @RequestParam(value = "inicio", required = false) String start,
            @RequestParam(value = "fim", required = false) String end
    ) {
        try {
            ExtractDTO extract = this.accountService.getExtract(accountNumber, start, end, userCPF);
            return ResponseEntity.status(HttpStatus.OK).body(extract);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{accountNumber}/cpf")
    String getCPF(@PathVariable String accountNumber) {
        try {
            return this.accountService.getCPF(accountNumber);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{accountNumber}/balance")
    BigDecimal getBalance(@PathVariable String accountNumber) {
        try {
            return this.accountService.getBalance(accountNumber);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/managers/{cpf}/")
    List<AccountData> getAccountsByManager(@PathVariable String cpf) {
        try {
            return this.accountService.getAccountsByManager(cpf);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/managers")
    List<AccountsByManagerDTO> getAccountsCount() {
        try {
            return this.accountService.getAccountsCountByManager();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
