package com.bantads.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

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
    private Long clientId;

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

    @Column(nullable = false, length = 2)
    private String state;

    @Column(nullable = false, length = 30)
    private String street;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = true, length = 30, name = 'additional_info')
    private String additionalInfo;

    @OneToMany(mappedBy = "client")
    private List<Request> requests;
}