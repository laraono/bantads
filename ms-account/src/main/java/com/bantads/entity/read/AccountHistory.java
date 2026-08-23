package com.bantads.entity.read;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "account_history", schema = "request_db")
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

    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, name = "origin_client_cpf")
    private String originClientCPF;

    @Column(nullable = false, name = "origin_client_name")
    private String originClientName;

    @Column(nullable = true, name = "destination_client_cpf")
    private String destinationClientCPF;

    @Column(nullable = true, name = "destination_client_name")
    private String destinationClientName;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

    @Column(nullable = false, name = "manager_cpf")
    private String managerCPF;

    @Column(nullable = false, name = "manager_name")
    private String managerName;
}