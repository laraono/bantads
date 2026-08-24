package com.bantads.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateManagerDTO {
    private String name;
    private String email;
    private String cpf;
    private String phone;
}
