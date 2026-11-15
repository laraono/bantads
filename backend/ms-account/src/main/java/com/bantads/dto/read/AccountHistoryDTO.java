package com.bantads.dto.read;

import com.bantads.entity.read.TransactionType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountHistoryDTO {
    private String accountNumber;

    private TransactionType type;

    private String originClientCPF;

    private String originClientName;

    private String destinationClientCPF;

    private String destinationClientName;

    private BigDecimal amount;

    private String managerCPF;

    private String managerName;
}
