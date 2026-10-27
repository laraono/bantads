package com.bantads.dto;

import java.math.BigDecimal;

import com.bantads.entity.Client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseDTO {

    public Long id;

    public String nome;

    public String email;

    public String cpf;

    public String telefone;

    public BigDecimal salario;

    public AddressResponseDTO endereco;

    public String status;

    public static ClientResponseDTO from(Client client) {
        if (client == null) {
            return null;
        }
        return ClientResponseDTO.builder()
                .id(client.getClientId())
                .nome(client.getName())
                .email(client.getEmail())
                .cpf(client.getCpf())
                .telefone(client.getPhone())
                .salario(client.getSalary())
                .endereco(AddressResponseDTO.from(client.getAddress()))
                .status(client.getApprovedStatus())
                .build();
    }
}
