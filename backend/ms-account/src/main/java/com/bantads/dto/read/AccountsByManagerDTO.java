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
    private Long totalAccounts;
    private BigDecimal totalBalance; // não é string pois não vai ao json e front
}
