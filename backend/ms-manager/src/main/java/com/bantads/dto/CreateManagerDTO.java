package com.bantads.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateManagerDTO {
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String senha;
}
