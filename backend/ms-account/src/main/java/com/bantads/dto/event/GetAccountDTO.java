package com.bantads.dto.event;

import com.bantads.entity.read.AccountData;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAccountDTO {


    private String numero;
    private String cpfCliente;
    private String saldo;
    private String cpfGerente;

    public static GetAccountDTO from(AccountData account) {
        return GetAccountDTO.builder()
                .numero(account.getAccountNumber())
                .cpfCliente(account.getClientCPF())
                .saldo(account.getBalance().toPlainString())
                .cpfGerente(account.getManagerCPF())
                .build();
    }
}
