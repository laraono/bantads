package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.event.GetAccountDTO;
import com.bantads.dto.event.RabbitCommandDTO;
import com.bantads.dto.read.*;
import com.bantads.entity.event.EventType;
import com.bantads.entity.read.AccountData;
import com.bantads.entity.read.Request;
import com.bantads.entity.read.TransactionType;
import com.bantads.repository.read.RequestRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AccountService {

    @Autowired
    private AccountDataService accountDataService;

    @Autowired
    private AccountHistoryService accountHistoryService;

    @Autowired
    private RequestRepository requestRepository;

    public String getCPF(String accountNumber) {
        return this.accountDataService.getCPF(accountNumber);
    }

    public BigDecimal getBalance(String accountNumber) {
        return this.accountDataService.getBalance(accountNumber);
    }

    public List<AccountData> getAccountsByManager(String managerCPF) {
        return this.accountDataService.getAccountsByManager(managerCPF);
    }

    public List<AccountsByManagerDTO> getAccountsCountByManager() {
        return this.accountDataService.getAccountsCountByManager();
    }

    public GetAccountDTO getAccountData(String accountNumber) {
        AccountData account = this.accountDataService.getAccountData(accountNumber);
        return GetAccountDTO.builder()
                .numero(account.getAccountNumber())
                .cpfCliente(account.getClientCPF())
                .build();
    }

    public RabbitQueryDTO createAccountData(RabbitCommandDTO body) {
        Map<String, Object> payload = body.getPayload();
        EventType eventType = body.getType();

        RabbitQueryDTO answer = RabbitQueryDTO.builder()
            .type(EventType.CREATED)
            .build();

        String value = (String) payload.get("valor");
        String clientCpf = (String) payload.get("cpfCliente");
        String managerCpf = (String) payload.get("cpfGerente");

        if(
            body.getAccountNumber() == null || clientCpf == null ||
            value == null || managerCpf == null || eventType == null
        ) {
            answer.setError("missing information");
            answer.setStatus(Status.FAILURE);
            answer.setTimestamp(new Date());

            return answer;
        }

        if(eventType != EventType.CREATED) {
            answer.setError("wrong request");
            answer.setStatus(Status.FAILURE);
            answer.setTimestamp(new Date());

            return answer;
        }

        AccountData accountData =  AccountData.builder()
            .accountNumber(body.getAccountNumber())
            .clientCPF(clientCpf)
            .balance(new BigDecimal(value))
            .managerCPF(managerCpf)
            .build();

        AccountData newAccount = this.accountDataService.createAccountData(accountData);


        answer.setTimestamp(new Date());
        answer.setStatus(Status.SUCCESS);
        answer.setPayload(this.toMap(newAccount));

        return answer;
    }

    public RabbitQueryDTO updateAccountBalance(RabbitCommandDTO body) {
        Map<String, Object> payload = body.getPayload();
        EventType eventType = body.getType();

        String accountNumber = body.getAccountNumber();
        String textValue = (String) payload.get("valor");

        RabbitQueryDTO answer = RabbitQueryDTO.builder()
            .type(body.getType())
            .build();

        if(
            textValue == null || eventType == null || accountNumber.isEmpty() || accountNumber == null
        ) {
            answer.setError("missing information");
            answer.setStatus(Status.FAILURE);
            answer.setTimestamp(new Date());

            return answer;
        }

        if(eventType == EventType.CREATED || eventType == EventType.UPDATEMANAGER) {
            answer.setError("wrong request");
            answer.setStatus(Status.FAILURE);
            answer.setTimestamp(new Date());

            return answer;
        }

        String error = this.createHistory(accountNumber, body);

        if(!error.isEmpty()) {
            answer.setError(error);
            answer.setStatus(Status.FAILURE);
            answer.setTimestamp(new Date());

            return answer;
        }

        BigDecimal value = new BigDecimal(textValue);

        if(eventType == EventType.WITHDRAW || eventType == EventType.ORIGINTRANSFER) value.negate();

        AccountData updatedAccount = this.accountDataService.updateAccountBalance(value, accountNumber);

        answer.setTimestamp(new Date());
        answer.setStatus(Status.SUCCESS);
        answer.setPayload(this.toMap(updatedAccount));

        return answer;
    }

    public RabbitQueryDTO updateAccountManager(RabbitCommandDTO body) {
        Map<String, Object> payload = body.getPayload();
        EventType eventType = body.getType();

        String accountNumber = body.getAccountNumber();
        String managerCpf = (String) payload.get("cpfGerente");

        RabbitQueryDTO answer = RabbitQueryDTO.builder()
            .type(body.getType())
            .build();

        if(
            managerCpf == null || eventType == null ||
            accountNumber == null || accountNumber.isEmpty()
        ) {
            answer.setError("missing information");
            answer.setStatus(Status.FAILURE);
            answer.setTimestamp(new Date());

            return answer;
        }

        if(eventType == EventType.UPDATEMANAGER) {
            answer.setError("wrong request");
            answer.setStatus(Status.FAILURE);
            answer.setTimestamp(new Date());

            return answer;
        }

        AccountData updatedAccount = this.accountDataService.updateAccountManager(managerCpf, accountNumber);

        answer.setTimestamp(new Date());
        answer.setStatus(Status.SUCCESS);
        answer.setPayload(this.toMap(updatedAccount));

        return answer;
    }

    public Map<String, Object> toMap(AccountData account) {
        Map<String, Object> map = new HashMap<>();

        if (account.getAccountNumber() != null) {
            map.put("accountNumber", account.getAccountNumber());
        }
        if (account.getClientCPF() != null) {
            map.put("clientCPF", account.getClientCPF());
        }
        if (account.getManagerCPF() != null) {
            map.put("managerCPF", account.getManagerCPF());
        }
        if (account.getBalance() != null) {
            map.put("balance", account.getBalance().toPlainString());
        }

        return map;
    }

    public String createHistory(String accountNumber, RabbitCommandDTO body) {
        Map<String, Object> payload = body.getPayload();
        AccountHistoryDTO ac = new AccountHistoryDTO();
        TransactionType type;

        String managerName = (String) payload.get("nomeGerente");
        String managerCpf = (String) payload.get("cpfGerente");

        Map<String, String> origin = (Map<String, String>) payload.get("origem");
        Map<String, String> destination = (Map<String, String>) payload.get("destino");

        String originClientName = origin.get("nome");
        String originClientCpf = (String) payload.get("clienteCpf");
        String textValue = origin.get("valor") == null ? origin.get("valor") : (String) payload.get("valor");

        String destinationClientName = destination.get("nome");
        String destinationClientAccount = destination.get("conta");
        String destinationClientCpf = destination.get("cpf");


        if(
            accountNumber == null || originClientCpf == null ||
            textValue == null || body.getType() == null
        ) {
            String error = "information missing";
            return error;
        }

        switch(body.getType()) {
            case DEPOSIT:
                type = TransactionType.DEPOSIT;
                ac.setType(type);
                break;
            case WITHDRAW:
                type = TransactionType.WITHDRAW;
                ac.setType(type);
                break;
            case ORIGINTRANSFER:
            case DESTINATIONTRANSFER:
                type = TransactionType.TRANSFER;
                ac.setType(type);
                break;
        }

        ac.setAccountNumber(accountNumber);
        ac.setAmount(new BigDecimal(textValue));
        ac.setOriginClientCPF(originClientCpf);
        ac.setOriginClientName(originClientName);
        ac.setManagerCPF(managerCpf);
        ac.setManagerName(managerName);
        ac.setDestinationClientCPF(destinationClientCpf);
        ac.setDestinationClientName(destinationClientName);

        this.accountHistoryService.createAcountHistory(ac);

        ac.setAccountNumber(destinationClientAccount);
        ac.setManagerName("");
        ac.setManagerCPF("");

        this.accountHistoryService.createAcountHistory(ac);

        return "";
    }



    public ExtractDTO getExtract(String accountNumber, String start, String end, String userCPF) {
        return this.accountHistoryService.getExtract(accountNumber, start, end, userCPF);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleCommand(RabbitCommandDTO command) {

        String textEventId = (String) command.getPayload().get("eventId");
        String textVersion = (String) command.getPayload().get("version");

        UUID eventId = UUID.fromString(textEventId);
        Long version = Long.parseLong(textVersion);

        boolean alreadyProcessed = requestRepository.existsByVersionAndEventId(version, eventId);

        if (alreadyProcessed) {
            return;
        }

        EventType eventType = command.getType();

        switch (eventType) {
            case EventType.CREATED:
                this.createAccountData(command);
                break;
            case EventType.UPDATEMANAGER:
                this.updateAccountManager(command);
                break;
            default:
                this.updateAccountBalance(command);
        }


        Request req = Request.builder()
                .eventId(eventId)
                .version(version)
                .build();

        requestRepository.save(req);
    }


}
