package com.bantads.dto.read;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsByManagerDTO {
    private String managerCPF;
    private long totalAccounts;
    private BigDecimal totalBalance;
}
