package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.event.RabbitCommandDTO;
import com.bantads.dto.read.AccountHistoryDTO;
import com.bantads.dto.read.AccountsByManagerDTO;
import com.bantads.dto.read.RabbitQueryDTO;
import com.bantads.dto.read.Status;
import com.bantads.entity.event.EventType;
import com.bantads.entity.read.AccountData;
import com.bantads.entity.read.Request;
import com.bantads.entity.read.TransactionType;
import com.bantads.repository.read.RequestRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public AccountData getAccountData(String accountNumber) {
        return this.accountDataService.getAccountData(accountNumber);
    }

    public RabbitQueryDTO createAccountData(RabbitCommandDTO body) {
        Map<String, String> payload = body.getPayload();
        EventType eventType = body.getType();

        RabbitQueryDTO answer = RabbitQueryDTO.builder()
            .type(EventType.CREATED)
            .build();

        if(
            payload.get("accountNumber") == null || payload.get("originClientCPF") == null ||
            payload.get("value") == null || payload.get("managerCPF") == null ||
            eventType == null
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
            .accountNumber(payload.get("accountNumber"))
            .clientCPF(payload.get("originClientCPF"))
            .balance(new BigDecimal(payload.get("value")))
            .managerCPF(payload.get("managerCPF"))
            .build();

        AccountData newAccount = this.accountDataService.createAccountData(accountData);


        answer.setTimestamp(new Date());
        answer.setStatus(Status.SUCCESS);
        answer.setPayload(this.toMap(newAccount));

        return answer;
    }

    public RabbitQueryDTO updateAccountBalance(RabbitCommandDTO body) {
        Map<String, String> payload = body.getPayload();
        EventType eventType = body.getType();
        String accountNumber = body.getAccountNumber();

        RabbitQueryDTO answer = RabbitQueryDTO.builder()
            .type(body.getType())
            .build();

        if(
            payload.get("value") == null || eventType == null ||
            accountNumber.isEmpty() || accountNumber == null
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

        BigDecimal value = new BigDecimal(payload.get("value"));

        if(eventType == EventType.WITHDRAW || eventType == EventType.ORIGINTRANSFER) value.negate();

        AccountData updatedAccount = this.accountDataService.updateAccountBalance(value, accountNumber);

        answer.setTimestamp(new Date());
        answer.setStatus(Status.SUCCESS);
        answer.setPayload(this.toMap(updatedAccount));

        return answer;
    }

    public RabbitQueryDTO updateAccountManager(RabbitCommandDTO body) {
        Map<String, String> payload = body.getPayload();
        EventType eventType = body.getType();
        String accountNumber = body.getAccountNumber();

        RabbitQueryDTO answer = RabbitQueryDTO.builder()
            .type(body.getType())
            .build();

        if(
            payload.get("managerCPF") == null || eventType == null ||
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

        AccountData updatedAccount = this.accountDataService.updateAccountManager(payload.get("managerCPF"), accountNumber);

        answer.setTimestamp(new Date());
        answer.setStatus(Status.SUCCESS);
        answer.setPayload(this.toMap(updatedAccount));

        return answer;
    }

    public Map<String, String> toMap(AccountData account) {
        Map<String, String> map = new HashMap<>();

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
        Map<String, String> payload = body.getPayload();
        AccountHistoryDTO ac = new AccountHistoryDTO();
        TransactionType type;

        if(
            payload.get("accountNumber") == null || payload.get("originClientCPF") == null ||
            payload.get("originClientName") == null || payload.get("managerName") == null ||
            payload.get("value") == null || payload.get("managerCPF") == null ||
            body.getType() == null
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
        ac.setAmount(new BigDecimal(payload.get("value")));
        ac.setOriginClientCPF(payload.get("originClientCPF"));
        ac.setOriginClientName(payload.get("originClientName"));
        ac.setManagerCPF(payload.get("managerCPF"));
        ac.setManagerName(payload.get("managerName"));
        ac.setDestinationClientCPF(payload.get("destinationClientCPF"));
        ac.setDestinationClientName(payload.get("destinationClientName"));

        this.accountHistoryService.createAcountHistory(ac);

        return "";
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleCommand(RabbitCommandDTO command) {

        UUID eventId = UUID.fromString(command.getPayload().get("eventId"));
        Long version = Long.parseLong(command.getPayload().get("version"));

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
