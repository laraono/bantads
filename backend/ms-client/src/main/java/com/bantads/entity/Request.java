package com.bantads.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "request_id")
    private Long requestId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(nullable = false)
    @Builder.Default
    private String status = RequestStatus.PENDING.getLabel();

    @Column(nullable = true, length = 255, name = "rejection_reason")
    private String rejectionReason;

    @Column(nullable = true, name = "rejection_at")
    private Date rejectionAt;
    
    @Column(nullable = true, name = "approved_at")
    private Date approvedAt;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal salary;

    @Column(nullable = false, length = 9)
    private String cep;

    @Column(nullable = false, length = 30)
    private String city;

    @Column(nullable = false, length = 30)
    private String street;

    @Column(nullable = false)
    private String number;

    @Column(nullable = true, length = 30, name = "additional_info")
    private String additionalInfo;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;
}