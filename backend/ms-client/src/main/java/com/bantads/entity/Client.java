package com.bantads.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "client_id")
    public Long clientId;

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

    @OneToOne(mappedBy = "client")
    @JsonIgnore
    private Request request;
    
    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;
    
    @Builder.Default
    @Column(name = "approved_status", nullable = false)
    private String approvedStatus = "PENDENTE";
    
    
}