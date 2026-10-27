package com.bantads.dto;

import jakarta.validation.constraints.NotBlank;
import com.bantads.entity.Address;

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
public class AddressResponseDTO {

    public String logradouro;

    public String numero;

    public String complemento;

    public String cep;

    public String cidade;

    public String uf;

    public static AddressResponseDTO from(Address address) {
        if (address == null) {
            return null;
        }
        return AddressResponseDTO.builder()
                .cep(address.getCep())
                .cidade(address.getCity())
                .uf(address.getState().getUf())
                .logradouro(address.getStreet())
                .numero(address.getNumber())
                .complemento(address.getAdditionalInfo())
                .build();
    }
}
