package com.bantads.dto.event;

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
}
