package com.bantads.entity.read;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "account_data", schema = "request_db")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountData {
    @Id
    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "client_cpf", nullable = false)
    private String clientCPF;

    @Column(name = "manager_cpf", nullable = false)
    private String managerCPF;

    @Column(nullable = false)
    private BigDecimal balance;
}
