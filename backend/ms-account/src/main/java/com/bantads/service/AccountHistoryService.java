package com.bantads.service;

import com.bantads.dto.read.AccountHistoryDTO;
import com.bantads.dto.read.ExtractDTO;
import com.bantads.dto.read.HistoryInstanceDTO;
import com.bantads.entity.event.Event;
import com.bantads.entity.event.EventType;
import com.bantads.entity.read.AccountHistory;
import com.bantads.entity.read.TransactionType;
import com.bantads.repository.read.AccountHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service

// Gerencia histórico de transações
public class AccountHistoryService {

    @Autowired
    private AccountHistoryRepository accountHistoryRepository;

    public AccountHistory createAcountHistory(AccountHistoryDTO ac) {
        AccountHistory accountHistory = AccountHistory.builder()
            .accountNumber(ac.getAccountNumber())
            .amount(ac.getAmount())
            .createdAt(LocalDateTime.now())
            .destinationClientCPF(ac.getDestinationClientCPF())
            .destinationClientName(ac.getDestinationClientName())
            .managerCPF(ac.getManagerCPF())
            .managerName(ac.getManagerName())
            .originClientCPF(ac.getOriginClientCPF())
            .originClientName(ac.getOriginClientName())
            .type(ac.getType())
            .build();

        return accountHistoryRepository.save(accountHistory);
    }

    private LocalDateTime parseDateTime(String dateStr, boolean isStart) {
        if (isStringNull(dateStr)) {
            return isStart
                ? LocalDate.now().minusDays(30).atStartOfDay()
                : LocalDate.now().atTime(LocalTime.MAX);
        }

        String trimmed = dateStr.trim();

        if (trimmed.length() == 10) {
            LocalDate localDate = LocalDate.parse(trimmed);
            return isStart ? localDate.atStartOfDay() : localDate.atTime(LocalTime.MAX);
        }

        if (trimmed.contains(" ") && !trimmed.contains("T")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(trimmed, formatter);
        }

        return LocalDateTime.parse(trimmed);
    }

    public ExtractDTO getExtract(String accountNumber, String start, String end, String userCPF) {
        LocalDateTime startDate = parseDateTime(start, true);
        LocalDateTime endDate = parseDateTime(end, false);

        this.checkDate(startDate, endDate);

        ExtractDTO extract = new ExtractDTO();
        BigDecimal balance = this.getInitialBalance(accountNumber, startDate, userCPF);
        extract.setSaldoAbertura(balance.toPlainString());

        List<AccountHistory> accountHistories = this.accountHistoryRepository
                .findAllByAccountNumberAndCreatedAtBetween(accountNumber, startDate, endDate);

        List<HistoryInstanceDTO> history = new ArrayList<>();
        
        for(AccountHistory accountHistory : accountHistories) {
            HistoryInstanceDTO instance = HistoryInstanceDTO.builder()
                    .historyId(accountHistory.getHistoryId())
                    .accountNumber(accountNumber)
                    .amount(accountHistory.getAmount().setScale(2, RoundingMode.HALF_EVEN).toPlainString())
                    .createdAt(accountHistory.getCreatedAt())
                    .destinationClientCPF(accountHistory.getDestinationClientCPF())
                    .destinationClientName(accountHistory.getDestinationClientName())
                    .managerCPF(accountHistory.getManagerCPF())
                    .managerName(accountHistory.getManagerName())
                    .originClientCPF(accountHistory.getOriginClientCPF())
                    .originClientName(accountHistory.getOriginClientName())
                    .type(accountHistory.getType())
                    .build();
            history.add(instance);
        }
        extract.setMovimentacoes(history);
        return extract;
    }

    public BigDecimal getInitialBalance(String accountNumber, LocalDateTime start, String userCPF) {
        List<AccountHistory> history = this.accountHistoryRepository.findAllByAccountNumberAndCreatedAtLessThan(accountNumber, start);

        Iterator<AccountHistory> historyIterator = history.iterator();

        BigDecimal balance = new BigDecimal(0);

        while(historyIterator.hasNext()) {
            AccountHistory ac = historyIterator.next();
            TransactionType type = ac.getType();
            BigDecimal value = ac.getAmount();

            if(type == TransactionType.DEPOSIT) {
                balance = balance.add(value);
            }

            if(type == TransactionType.WITHDRAW) {
                balance = balance.subtract(value);
            }

            if(type == TransactionType.TRANSFER && Objects.equals(ac.getDestinationClientCPF(), userCPF)) {
                balance = balance.add(value);
            }

            if(type == TransactionType.TRANSFER && Objects.equals(ac.getOriginClientCPF(), userCPF)) {
                balance = balance.subtract(value);
            }
        }

        return balance;
    }

    public void checkDate(LocalDateTime start, LocalDateTime end) {
        long daysBetween = Duration.between(start, end).toDays();

        if(daysBetween > 365) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Filtro não pode ter diferença maior que 365 dias");
        }

    }

    public boolean isStringNull(String str) {
        if(str == null || str.isEmpty()) return true;
        return false;
    }
}
