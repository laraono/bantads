package com.bantads.entity.read;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "account_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "history_id")
    private Long historyId;

    @Column(nullable = false, name = "account_number", length = 4)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, name = "origin_client_cpf")
    private String originClientCPF;

    @Column(nullable = true, name = "origin_client_name")
    private String originClientName;

    @Column(nullable = true, name = "destination_client_cpf")
    private String destinationClientCPF;

    @Column(nullable = true, name = "destination_client_name")
    private String destinationClientName;

    @Column(nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true, name = "manager_cpf")
    private String managerCPF;

    @Column(nullable = true, name = "manager_name")
    private String managerName;
}