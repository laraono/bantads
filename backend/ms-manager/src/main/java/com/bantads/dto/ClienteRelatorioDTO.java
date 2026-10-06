package com.bantads.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRelatorioDTO {
    private String cpf;
    private String nome;
    private String email;
    private BigDecimal salario;
    private String numeroConta;
    private BigDecimal saldo;
    private String cpfGerente;
    private String nomeGerente;
}
