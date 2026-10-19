package com.bantads.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTO {

    @NotBlank(message = "Email obrigatório")
    @Email(message = "Email inválido")
    String email;

    @NotBlank(message = "Senha obrigatória")
    String senha;
}
