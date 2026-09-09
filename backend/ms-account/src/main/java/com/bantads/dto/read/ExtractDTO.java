package com.bantads.dto.read;

import com.bantads.entity.read.AccountHistory;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractDTO {
    private String saldoAbertura;
    private List<AccountHistory> movimentacoes;
}
