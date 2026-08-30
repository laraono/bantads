/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bantads.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "address", schema = "client_db")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "address_id")
    private Long addressId;
    
    @Column(nullable = false, length = 9)
    private String cep;

    @Column(nullable = false, length = 30)
    private String city;

    @Column(nullable = false, length = 30)
    private String street;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = true, length = 30, name = "additional_info")
    private String additionalInfo;
    
    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;
    
    @OneToMany(mappedBy = "address")
    private List<Client> clients;
    
}
