package com.bantads.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
@Builder
@AllArgsConstructor 
public class RejectRequestDTO {
    @NotBlank(message = "Motivo é obrigatório")
    private String motivo; 
}
