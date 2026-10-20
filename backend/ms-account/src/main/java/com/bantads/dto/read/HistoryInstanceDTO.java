package com.bantads.dto.read;

import java.time.LocalDateTime;

import com.bantads.entity.read.TransactionType;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class HistoryInstanceDTO {
    
    private String accountNumber;

    private TransactionType type;

    private String originClientCPF;

    private String originClientName;

    private String destinationClientCPF;

    private String destinationClientName;

    private String amount;

    private String managerCPF;

    private String managerName;

    private Long historyId;

    private LocalDateTime createdAt;

}
