package com.bantads.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateManagerDTO {

    @NotNull
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "^\\d{11}$", message = "CPF inválido")
    private String cpf;

    @NotNull
    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotNull
    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;

    @NotNull
    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}
