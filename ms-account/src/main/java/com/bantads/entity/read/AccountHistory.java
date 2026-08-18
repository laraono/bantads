package com.bantads.entity.read;

import java.math.BigDecimal;
import java.util.Date;

public class AccountHistory {
    private Long historyId;
    private String accountNumber;
    private TransactionType type;
    private String originClientCPF;
    private String originClientName;
    private String destinationClientCPF;
    private String destinationClientName;
    private BigDecimal amount;
    private Date createdAt;
}