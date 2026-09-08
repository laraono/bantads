package com.bantads.dto.read;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsByManagerDTO {
    private String managerId;
    private int count;
}
