package com.bantads.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.Thread.State;
import java.math.BigDecimal;

import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.entity.RequestStatus;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestResponseDTO {

    public static RequestResponseDTO from(Request request) {
        AddressResponseDTO endereco = AddressResponseDTO.builder()
                .cep(request.getCep())
                .cidade(request.getCity())
                .uf(request.getState().getUf())
                .logradouro(request.getStreet())
                .numero(request.getNumber())
                .complemento(request.getAdditionalInfo())
                .build();

        return RequestResponseDTO.builder()
                .id(request.getRequestId())
                .nome(request.getName())
                .email(request.getEmail())
                .cpf(request.getCpf())
                .telefone(request.getPhone())
                .salario(request.getSalary())
                .endereco(endereco)
                .cliente(ClientResponseDTO.from(request.getClient()))
                .status(request.getStatus())
                .motivo(request.getRejectionReason())
                .build();
    }

    public Long id;
    public String nome;
    public String email;
    public String cpf;
    public String telefone;
    public BigDecimal salario;
    public AddressResponseDTO endereco;
    public ClientResponseDTO cliente;
    public RequestStatus status;
    public String motivo;

}