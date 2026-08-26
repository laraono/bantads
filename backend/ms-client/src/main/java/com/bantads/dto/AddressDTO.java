/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bantads.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author lenovo
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    private String cep;

    private String city;

    private String street;

    private Integer number;

    private String additionalInfo;
    
    private Long stateId;

}
