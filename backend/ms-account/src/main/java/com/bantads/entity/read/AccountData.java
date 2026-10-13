package com.bantads.entity.read;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_data")
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
