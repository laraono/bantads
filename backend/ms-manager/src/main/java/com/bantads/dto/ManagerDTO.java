package com.bantads.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerDTO {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private boolean ativo;
}
