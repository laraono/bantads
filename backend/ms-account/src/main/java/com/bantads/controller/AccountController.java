package com.bantads.controller;

import com.bantads.assemblers.AccountDataModelAssembler;
import com.bantads.dto.event.GetAccountDTO;
import com.bantads.dto.read.ExtractDTO;
import com.bantads.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountDataModelAssembler assembler;

    @GetMapping
    @ResponseStatus (HttpStatus.OK)
    public CollectionModel<EntityModel<GetAccountDTO>> listAccounts(
            @RequestParam(name = "cpf", required = false) List<String> cpf) {
        try {
            
            List<GetAccountDTO> accounts = this.accountService.listAccounts(cpf);

            return assembler.toCollectionModel(accounts);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/cpf/{cpf}")
    @ResponseStatus (HttpStatus.OK)
    public EntityModel<GetAccountDTO> getAccountDataByCpf(@PathVariable String cpf,
            @RequestHeader("x-user-cpf") String userCPF) {
        try {
            GetAccountDTO account = this.accountService.getAccountDataByCpf(cpf, userCPF);
            return assembler.toModel(account);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{accountNumber}")
    @ResponseStatus (HttpStatus.OK)
    public EntityModel<GetAccountDTO> getAccountData(@PathVariable String accountNumber) {
        try {
            GetAccountDTO account = this.accountService.getAccountData(accountNumber);
            return assembler.toModel(account);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{accountNumber}/extract")
    public ResponseEntity<ExtractDTO> getExtract(
            @RequestHeader("x-user-cpf") String userCPF,
            @PathVariable String accountNumber,
            @RequestParam(value = "inicio", required = false) String start,
            @RequestParam(value = "fim", required = false) String end) {
        try {
            ExtractDTO extract = this.accountService.getExtract(accountNumber, start, end, userCPF);
            return ResponseEntity.status(HttpStatus.OK).body(extract);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{accountNumber}/cpf")
    @ResponseStatus (HttpStatus.OK)
    public String getCPF(@PathVariable String accountNumber) {
        try {
            return this.accountService.getCPF(accountNumber);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{accountNumber}/balance")
    @ResponseStatus (HttpStatus.OK)
    public String getBalance(@PathVariable String accountNumber) {
        try {
            return this.accountService.getBalance(accountNumber).toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/managers/{cpf}/")
    public CollectionModel<EntityModel<GetAccountDTO>> getAccountsByManager(@PathVariable String cpf) {
        try {
            List<GetAccountDTO> accounts = this.accountService.getAccountsByManager(cpf);
            return assembler.toCollectionModel(accounts);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
