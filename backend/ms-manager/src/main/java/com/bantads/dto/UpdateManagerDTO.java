package com.bantads.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateManagerDTO {
    // não tem check porque a alteração deles é opcional
    private String nome;
    private String telefone;
}
