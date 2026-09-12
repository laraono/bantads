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
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    @Transactional()
    public GetAccountDTO getAccountData(String accountNumber) {
        AccountData account = this.accountDataService.getAccountData(accountNumber);

        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account " + accountNumber + " not found");
        }

        return GetAccountDTO.builder()
            .numero(account.getAccountNumber())
            .cpfCliente(account.getClientCPF())
            .saldo(account.getBalance().toPlainString())
            .build();
    }

    public RabbitQueryDTO createAccountData(RabbitCommandDTO body) {
        try{
            Map<String, Object> payload = body.getPayload();
            EventType eventType = body.getType();

            RabbitQueryDTO answer = RabbitQueryDTO.builder()
                .type(EventType.CREATED)
                .build();

            String value = String.valueOf(payload.get("valor"));
            String clientCpf = String.valueOf(payload.get("cpfCliente"));
            String managerCpf = String.valueOf(payload.get("cpfGerente"));

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
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public RabbitQueryDTO updateAccountBalance(RabbitCommandDTO body) {
        try {
            Map<String, Object> payload = body.getPayload();
            EventType eventType = body.getType();

            String accountNumber = body.getAccountNumber();
            String textValue = String.valueOf(payload.get("valor"));

            RabbitQueryDTO answer = RabbitQueryDTO.builder()
                .type(body.getType())
                .build();

            if (
                textValue == null || eventType == null || accountNumber.isEmpty() || accountNumber == null
            ) {
                answer.setError("missing information");
                answer.setStatus(Status.FAILURE);
                answer.setTimestamp(new Date());

                return answer;
            }

            if (eventType == EventType.CREATED || eventType == EventType.UPDATEMANAGER) {
                answer.setError("wrong request");
                answer.setStatus(Status.FAILURE);
                answer.setTimestamp(new Date());

                return answer;
            }

            String error = this.createHistory(accountNumber, body);

            if (!error.isEmpty()) {
                answer.setError(error);
                answer.setStatus(Status.FAILURE);
                answer.setTimestamp(new Date());

                return answer;
            }

            BigDecimal value = new BigDecimal(textValue);

            if (eventType == EventType.WITHDRAW || eventType == EventType.ORIGINTRANSFER) value = value.negate();

            AccountData updatedAccount = this.accountDataService.updateAccountBalance(value, accountNumber);

            answer.setTimestamp(new Date());
            answer.setStatus(Status.SUCCESS);
            answer.setPayload(this.toMap(updatedAccount));

            return answer;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public RabbitQueryDTO updateAccountManager(RabbitCommandDTO body) {
        try {
            Map<String, Object> payload = body.getPayload();
            EventType eventType = body.getType();

            String accountNumber = body.getAccountNumber();
            String managerCpf = String.valueOf(payload.get("cpfGerente"));

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
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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

        if (payload == null || body.getType() == null) {
            return "information missing";
        }

        AccountHistoryDTO ac = new AccountHistoryDTO();

        switch (body.getType()) {
            case DEPOSIT:
                ac.setType(TransactionType.DEPOSIT);
                break;
            case WITHDRAW:
                ac.setType(TransactionType.WITHDRAW);
                break;
            case ORIGINTRANSFER:
            case DESTINATIONTRANSFER:
                ac.setType(TransactionType.TRANSFER);
                break;
            default:
                return "unsupported transaction type";
        }

        Map<?, ?> origin = (Map<?, ?>) payload.get("origem");
        Map<?, ?> destination = (Map<?, ?>) payload.get("destino");

        String originClientName = (origin != null) ? String.valueOf(origin.get("nome")) : null;
        String originClientCpf = payload.get("clienteCpf") != null ? String.valueOf(payload.get("clienteCpf")) : null;

        String destinationClientName = (destination != null) ? String.valueOf(destination.get("nome")) : null;
        String destinationClientAccount = (destination != null) ? String.valueOf(destination.get("conta")) : null;
        String destinationClientCpf = (destination != null) ? String.valueOf(destination.get("cpf")) : null;

        Object rawValue = (origin != null && origin.get("valor") != null) 
                ? origin.get("valor") 
                : payload.get("valor");
        String textValue = rawValue != null ? String.valueOf(rawValue) : null;

        String managerName = payload.get("nomeGerente") != null ? String.valueOf(payload.get("nomeGerente")) : null;
        String managerCpf = payload.get("cpfGerente") != null ? String.valueOf(payload.get("cpfGerente")) : null;

        if (accountNumber == null || originClientCpf == null || textValue == null) {
            return "information missing";
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

        if (destinationClientAccount != null && ac.getType() == TransactionType.TRANSFER) {
            ac.setAccountNumber(destinationClientAccount);
            ac.setManagerName("");
            ac.setManagerCPF("");

            this.accountHistoryService.createAcountHistory(ac);
        }

        return "";
    }

    public ExtractDTO getExtract(String accountNumber, String start, String end, String userCPF) {
        return this.accountHistoryService.getExtract(accountNumber, start, end, userCPF);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleCommand(RabbitCommandDTO command) {

        String textEventId = String.valueOf(command.getPayload().get("eventId"));
        String textVersion = String.valueOf(command.getPayload().get("version"));

        UUID eventId = UUID.fromString(textEventId);
        Long version = Long.parseLong(textVersion);

        boolean alreadyProcessed = requestRepository.existsByVersionAndEventId(version, eventId);

        if (alreadyProcessed) {
            return;
        }

        Request req = Request.builder()
                .eventId(eventId)
                .version(version)
                .build();

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

        requestRepository.save(req);
    }


}
