package com.bantads.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDTO {

    @NotBlank(message = "Nome é obrigatório") 
    private String nome;

    @NotBlank(message = "E-mail é obrigatório") 
    @Email(message = "E-mail inválido") 
    private String email;
    
    @NotBlank(message = "CPF é obrigatório") 
    @Pattern(regexp = "^\\d{11}$", message = "CPF inválido")
    private String cpf;

    @NotBlank(message = "Telefone é obrigatório") 
    @Pattern(regexp = "^\\d{11}$", message = "Telefone inválido") 
    private String telefone;

    @NotNull(message = "Salário é obrigatório") 
    private BigDecimal salario;
    
    @NotNull(message = "Endereço é obrigatório") 
    private AddressDTO endereco;
    
}

