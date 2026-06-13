package com.app.Projeto.fiado.digital.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendaResumoDTO {

    private Long id;
    private String nomeCliente;
    private LocalDateTime dataVenda;
    private BigDecimal valorTotal;
    private BigDecimal saldoDevedor;
    private boolean ativa;
}